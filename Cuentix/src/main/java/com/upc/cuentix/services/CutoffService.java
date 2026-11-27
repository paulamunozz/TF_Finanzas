package com.upc.cuentix.services;

import com.upc.cuentix.config.ResourceAccess;
import com.upc.cuentix.entities.Cierre;
import com.upc.cuentix.entities.Cliente;
import com.upc.cuentix.entities.CuentaCredito;
import com.upc.cuentix.entities.Cuota;
import com.upc.cuentix.entities.LineaCierre;
import com.upc.cuentix.repositories.CuotaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CutoffService {
  static final ZoneId LIMA = ZoneId.of("America/Lima");
  private final EntityManager em;
  private final ResourceAccess access;
  private final CuotaRepository installments;

  private record Cutoff(YearMonth period, LocalDateTime at, LocalDate dueDate) {}

  public List<Integer> accountIds() {
    return em.createQuery("select c.id from CuentaCredito c", Integer.class).getResultList();
  }

  public void issue(Integer id, LocalDateTime now) {
    CuentaCredito account = em.find(CuentaCredito.class, id, LockModeType.PESSIMISTIC_WRITE);
    if (account == null) return;

    var terms = em.createQuery("""
        select q from Cuota q join fetch q.compra c where c.cuentaCredito.id = :id and c.fecha is not null
        order by c.fecha, c.horaCompra, c.id, q.num
        """, Cuota.class).setParameter("id", id).getResultList();
    var contracted = terms.stream().filter(term -> term.getCompra().getFechaCorte() != null)
        .collect(Collectors.toMap(Cuota::getId, term -> cutoffFor(term, account)));
    var existing = em.createQuery("select c from Cierre c where c.cuenta.id = :id", Cierre.class)
        .setParameter("id", id).getResultList().stream()
        .collect(Collectors.toMap(Cierre::getPeriodo, statement -> statement));
    YearMonth current = YearMonth.from(now);
    YearMonth first = terms.stream().map(term -> term.getCompra())
        .map(purchase -> YearMonth.from(Objects.requireNonNullElse(purchase.getFechaCorte(), purchase.getFecha())))
        .min(Comparator.naturalOrder()).orElse(current);

    for (YearMonth month = first; !month.isAfter(current); month = month.plusMonths(1)) {
      Cierre statement = existing.get(month.atDay(1));
      boolean adjustment = statement != null;
      if (statement == null) {
        Cutoff cutoff = defaultCutoff(account, month);
        for (Cutoff scheduled : contracted.values()) {
          if (scheduled.period().equals(month) && scheduled.at().isBefore(cutoff.at())) cutoff = scheduled;
        }
        if (cutoff.at().isAfter(now)) continue;
        statement = new Cierre();
        statement.setCuenta(account);
        statement.setPeriodo(month.atDay(1));
        statement.setCorte(cutoff.at());
        statement.setVencimiento(cutoff.dueDate());
        statement.setMoneda(BusinessApplicationService.currency(account));
        em.persist(statement);
      }

      var known = new HashSet<>(em.createQuery("select l.cuota.id from LineaCierre l where l.cierre = :c", Integer.class)
          .setParameter("c", statement).getResultList());
      for (Cuota term : terms) {
        if (known.contains(term.getId())
            || !belongsAtCutoff(term, contracted.get(term.getId()), statement, month, now)) continue;
        var line = new LineaCierre();
        line.setCierre(statement);
        line.setCuota(term);
        line.setAjuste(adjustment);
        line.setCapital(FinancialMath.money(term.getTotal()).subtract(FinancialMath.money(term.getIntereses())));
        line.setInteres(FinancialMath.money(term.getIntereses()));
        line.setMora(InstallmentAmounts.lateAt(term, statement.getVencimiento()));
        em.persist(line);
      }
    }
  }

  private static boolean belongsAtCutoff(Cuota term, Cutoff contracted, Cierre statement,
      YearMonth month, LocalDateTime now) {
    var purchase = term.getCompra();
    LocalDateTime purchaseAt = purchase.getFecha()
        .atTime(Objects.requireNonNullElse(purchase.getHoraCompra(), LocalTime.MIN));
    LocalDateTime cutoff = contracted != null && contracted.period().equals(month)
        && contracted.at().isAfter(statement.getCorte()) ? contracted.at() : statement.getCorte();
    if (cutoff.isAfter(now) || !purchaseAt.isBefore(cutoff)) return false;
    if (Boolean.TRUE.equals(term.getCancelado())
        && (term.getFechaPago() == null || term.getFechaPago().isBefore(cutoff.toLocalDate()))) return false;
    return contracted == null ? !InstallmentAmounts.dueDate(term).isAfter(statement.getVencimiento())
        : !contracted.period().isAfter(month);
  }

  private static Cutoff defaultCutoff(CuentaCredito account, YearMonth month) {
    LocalDate day = month.atDay(Math.min(account.getDiaCorte(), month.lengthOfMonth()));
    return new Cutoff(month, boundary(day, account.getHoraCorte()),
        FinancialMath.paymentDate(day, account.getDiaCorte(), account.getDiaPago()));
  }

  private static Cutoff cutoffFor(Cuota term, CuentaCredito account) {
    var purchase = term.getCompra();
    int cutoffDay = Objects.requireNonNullElse(purchase.getDiaCortePactado(), account.getDiaCorte());
    int paymentDay = Objects.requireNonNullElse(purchase.getDiaPagoPactado(), account.getDiaPago());
    LocalDate firstPayment = FinancialMath.paymentDate(purchase.getFechaCorte(), cutoffDay, paymentDay);
    LocalDate dueDate = InstallmentAmounts.dueDate(term);
    long months = ChronoUnit.MONTHS.between(YearMonth.from(firstPayment), YearMonth.from(dueDate));
    YearMonth period = YearMonth.from(purchase.getFechaCorte()).plusMonths(Math.max(0, months));
    LocalDate day = period.atDay(Math.min(cutoffDay, period.lengthOfMonth()));
    return new Cutoff(period, boundary(day, purchase.getHoraCortePactada()), dueDate);
  }

  private static LocalDateTime boundary(LocalDate date, LocalTime time) {
    return time == null ? date.plusDays(1).atStartOfDay() : date.atTime(time);
  }

  public record Entry(Integer id, Integer purchaseId, LocalDate purchaseDate, int number, LocalDate dueDate,
      BigDecimal capital, BigDecimal interest, BigDecimal graceInterest, BigDecimal late,
      LocalDate paidAt, BigDecimal paidLate, boolean adjustment) {}

  public record Report(Integer id, String customer, String currency, LocalDate paymentDay, List<Entry> entries) {}

  @Transactional(readOnly = true)
  public List<Report> reports(Integer businessId, YearMonth month) {
    access.business(businessId);
    return em.createQuery("""
        select c from Cierre c join fetch c.cuenta a join fetch a.cliente
        where a.negocio.id = :business and c.periodo = :period order by a.id
        """, Cierre.class).setParameter("business", businessId).setParameter("period", month.atDay(1))
        .getResultList().stream().map(this::report).toList();
  }

  private Report report(Cierre statement) {
    var lines = em.createQuery("""
        select l from LineaCierre l join fetch l.cuota q join fetch q.compra p
        where l.cierre = :statement order by p.fecha, p.horaCompra, p.id, q.num
        """, LineaCierre.class).setParameter("statement", statement).getResultList();
    var purchaseIds = lines.stream().map(line -> line.getCuota().getCompra().getId()).collect(Collectors.toSet());
    var amounts = InstallmentAmounts.byTerm(purchaseIds.isEmpty()
        ? List.of() : installments.findByCompra_IdIn(purchaseIds));
    var entries = lines.stream().map(line -> {
      var term = line.getCuota();
      var purchase = term.getCompra();
      var grace = amounts.get(term.getId()).graceInterest();
      var capitalizedGrace = "installments".equals(purchase.getModalidad()) ? grace : BigDecimal.ZERO;
      var paidLate = Boolean.TRUE.equals(term.getCancelado()) ? FinancialMath.money(term.getMoras()) : null;
      return new Entry(term.getId(), purchase.getId(), purchase.getFecha(), term.getNum(),
          InstallmentAmounts.dueDate(term), line.getCapital().subtract(capitalizedGrace),
          line.getInteres().add(capitalizedGrace), grace, line.getMora(), term.getFechaPago(),
          paidLate, line.isAjuste());
    }).toList();
    Cliente client = statement.getCuenta().getCliente();
    return new Report(statement.getId(), client.getNombre() + " " + client.getApellido(),
        statement.getMoneda(), statement.getVencimiento(), entries);
  }
}
