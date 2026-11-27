package com.upc.cuentix.services;

import com.upc.cuentix.config.ResourceAccess;
import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.CuentaCredito;
import com.upc.cuentix.entities.Cuota;
import com.upc.cuentix.entities.DetallePago;
import com.upc.cuentix.entities.ReciboPago;
import com.upc.cuentix.repositories.CuotaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupedPaymentService {
  private final EntityManager em;
  private final ResourceAccess access;
  private final CuotaRepository installments;

  public record Line(Integer installmentId, Integer purchaseId, int number, BigDecimal late,
                     BigDecimal interest, BigDecimal capital, BigDecimal graceInterest) {}

  public record Quote(LocalDate dueDate, LocalDate paidAt, String currency, List<Line> lines,
                      BigDecimal late, BigDecimal interest, BigDecimal capital, BigDecimal total) {}

  public record Confirmation(LocalDate paidAt, BigDecimal amount, List<Integer> installmentIds,
                             UUID requestKey) {}

  public record Receipt(Integer id, Quote payment) {}

  @Transactional(readOnly = true)
  public Quote quote(Integer accountId, LocalDate dueDate, LocalDate paidAt) {
    access.account(accountId);
    CuentaCredito account = em.find(CuentaCredito.class, accountId);
    return quote(account, dueDate, paidAt, pending(accountId, dueDate));
  }

  public Receipt confirm(Integer accountId, LocalDate dueDate, Confirmation request) {
    access.account(accountId);
    if (request.requestKey() == null || request.paidAt() == null || request.amount() == null
        || request.installmentIds() == null || request.installmentIds().isEmpty()
        || request.installmentIds().stream().anyMatch(Objects::isNull))
      throw new IllegalArgumentException("Indique fecha, importe, cuotas y clave de confirmación");

    CuentaCredito account = lockAccount(accountId);
    var previous = em.createQuery("""
        SELECT r FROM ReciboPago r WHERE r.cuenta.id = :account AND r.clave = :key
        """, ReciboPago.class).setParameter("account", accountId)
        .setParameter("key", request.requestKey()).getResultList();
    if (!previous.isEmpty()) {
      Receipt saved = receipt(previous.get(0));
      if (!saved.payment().dueDate().equals(dueDate) || !saved.payment().paidAt().equals(request.paidAt())
          || saved.payment().total().compareTo(request.amount()) != 0
          || !sameInstallments(request.installmentIds(), saved.payment().lines()))
        throw conflict("La clave de confirmación ya corresponde a otro pago");
      return saved;
    }

    List<Cuota> terms = pending(accountId, dueDate);
    if (terms.isEmpty()) throw conflict("El saldo del vencimiento cambió. Vuelva a cotizar.");
    Quote quote = quote(account, dueDate, request.paidAt(), terms);
    if (!sameInstallments(request.installmentIds(), quote.lines()))
      throw conflict("El saldo del vencimiento cambió. Vuelva a cotizar.");
    validateAmount(quote, request.amount());
    return save(account, quote, terms, request.requestKey());
  }

  void confirmLegacy(Cuota term, LocalDate paidAt, BigDecimal amount) {
    Integer accountId = term.getCompra().getCuentaCredito().getId();
    CuentaCredito account = lockAccount(accountId);
    LocalDate dueDate = InstallmentAmounts.dueDate(term);
    List<Cuota> terms = pending(accountId, dueDate);
    if (terms.stream().noneMatch(q -> q.getId().equals(term.getId())))
      throw new IllegalArgumentException("Cuota ya cancelada");
    if (terms.size() > 1)
      throw conflict("Registre el pago completo de las obligaciones del vencimiento");
    Quote quote = quote(account, dueDate, paidAt, terms);
    if (amount != null) validateAmount(quote, amount);
    save(account, quote, terms, UUID.randomUUID());
  }

  CuentaCredito lockAccount(Integer id) {
    CuentaCredito account = em.find(CuentaCredito.class, id);
    em.refresh(account, LockModeType.PESSIMISTIC_WRITE);
    return account;
  }

  private List<Cuota> pending(Integer account, LocalDate dueDate) {
    return installments.pendingAt(account, Date.valueOf(dueDate), Date.valueOf(dueDate.plusDays(1)));
  }

  private Quote quote(CuentaCredito account, LocalDate dueDate, LocalDate paidAt, List<Cuota> terms) {
    if (terms.isEmpty()) throw conflict("No hay obligaciones pendientes para este vencimiento");
    if (paidAt == null || paidAt.isAfter(LocalDate.now(CutoffService.LIMA))
        || terms.stream().anyMatch(term -> paidAt.isBefore(term.getCompra().getFecha())))
      throw new IllegalArgumentException("La fecha de pago debe estar entre la última compra del grupo y hoy");

    Set<Integer> termIds = terms.stream().map(Cuota::getId).collect(Collectors.toSet());
    Set<Integer> purchaseIds = terms.stream().map(term -> term.getCompra().getId()).collect(Collectors.toSet());
    List<Cuota> schedules = installments.findByCompra_IdIn(purchaseIds);
    var firstInGroup = terms.stream().collect(Collectors.toMap(
        term -> term.getCompra().getId(), Cuota::getNum, Math::min));
    if (schedules.stream().anyMatch(term -> !Boolean.TRUE.equals(term.getCancelado())
        && !termIds.contains(term.getId()) && term.getNum() < firstInGroup.get(term.getCompra().getId())))
      throw new IllegalArgumentException("Pague primero las cuotas anteriores");

    var amounts = InstallmentAmounts.byTerm(schedules);
    List<Line> lines = terms.stream().map(term -> {
      var split = amounts.get(term.getId());
      return new Line(term.getId(), term.getCompra().getId(), term.getNum(),
          InstallmentAmounts.lateAt(term, paidAt), split.interest(), split.capital(), split.graceInterest());
    }).toList();
    return totals(dueDate, paidAt, BusinessApplicationService.currency(account), lines);
  }

  private static boolean sameInstallments(List<Integer> ids, List<Line> lines) {
    return ids.size() == lines.size()
        && new HashSet<>(ids).equals(lines.stream().map(Line::installmentId).collect(Collectors.toSet()));
  }

  private static void validateAmount(Quote quote, BigDecimal amount) {
    if (quote.total().compareTo(amount) != 0)
      throw new IllegalArgumentException("El importe debe coincidir con el total exigible");
  }

  private Receipt save(CuentaCredito account, Quote quote, List<Cuota> terms, UUID key) {
    ReciboPago receipt = new ReciboPago();
    receipt.setCuenta(account);
    receipt.setClave(key);
    receipt.setVencimiento(quote.dueDate());
    receipt.setFechaPago(quote.paidAt());
    receipt.setMoneda(quote.currency());

    for (int i = 0; i < terms.size(); i++) {
      Cuota term = terms.get(i);
      Line line = quote.lines().get(i);
      DetallePago detail = new DetallePago();
      detail.setRecibo(receipt);
      detail.setCuota(term);
      detail.setMora(line.late());
      detail.setInteres(line.interest());
      detail.setCapital(line.capital());
      detail.setInteresGracia(line.graceInterest());
      receipt.getDetalles().add(detail);
    }

    // Impute the full payment in contractual order before releasing the reserved balance.
    for (DetallePago detail : receipt.getDetalles()) detail.getCuota().setMoras(detail.getMora().doubleValue());
    BigDecimal compensatory = sum(quote.lines(), Line::interest);
    BigDecimal capital = sum(quote.lines(), Line::capital);
    account.setDeuda(FinancialMath.money(account.getDeuda()).subtract(compensatory).subtract(capital)
        .max(BigDecimal.ZERO).doubleValue());
    for (Cuota term : terms) {
      term.setFechaPago(quote.paidAt());
      term.setCancelado(true);
    }
    for (Compra purchase : terms.stream().map(Cuota::getCompra).distinct().toList()) {
      purchase.setFinalCuotas(installments.findByCompra_Id(purchase.getId()).stream()
          .allMatch(term -> Boolean.TRUE.equals(term.getCancelado())));
    }
    em.persist(receipt);
    return new Receipt(receipt.getId(), quote);
  }

  private static Receipt receipt(ReciboPago saved) {
    List<Line> lines = saved.getDetalles().stream().map(detail -> {
      Cuota term = detail.getCuota();
      return new Line(term.getId(), term.getCompra().getId(), term.getNum(), detail.getMora(),
          detail.getInteres(), detail.getCapital(), detail.getInteresGracia());
    }).toList();
    return new Receipt(saved.getId(), totals(saved.getVencimiento(), saved.getFechaPago(), saved.getMoneda(), lines));
  }

  private static Quote totals(LocalDate dueDate, LocalDate paidAt, String currency, List<Line> lines) {
    BigDecimal late = sum(lines, Line::late);
    BigDecimal interest = sum(lines, Line::interest);
    BigDecimal capital = sum(lines, Line::capital);
    return new Quote(dueDate, paidAt, currency, lines, late, interest, capital, late.add(interest).add(capital));
  }

  private static BigDecimal sum(List<Line> lines, Function<Line, BigDecimal> component) {
    return lines.stream().map(component).reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  private static ResponseStatusException conflict(String message) {
    return new ResponseStatusException(HttpStatus.CONFLICT, message);
  }
}
