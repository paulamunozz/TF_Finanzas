package com.upc.cuentix.services;

import com.upc.cuentix.entities.*;
import com.upc.cuentix.config.ResourceAccess;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CutoffService {
    private final EntityManager em;
    private final BusinessApplicationService business;
    private final ResourceAccess access;
    static final ZoneId LIMA = ZoneId.of("America/Lima");

    public List<Integer> accountIds() {
        return em.createQuery("select c.id from CuentaCredito c", Integer.class).getResultList();
    }

    public void issue(Integer id, LocalDateTime now) {
        CuentaCredito account = em.find(CuentaCredito.class, id, LockModeType.PESSIMISTIC_WRITE);
        if (account == null) return;
        var terms = em.createQuery("select q from Cuota q join fetch q.compra c where c.cuentaCredito.id=:id order by c.fecha, c.id, q.num", Cuota.class)
                .setParameter("id", id).getResultList();
        var existing = em.createQuery("select c from Cierre c where c.cuenta.id=:id", Cierre.class)
                .setParameter("id",id).getResultList().stream().collect(Collectors.toMap(Cierre::getPeriodo,c -> c));
        YearMonth current = YearMonth.from(now);
        YearMonth first = terms.stream().map(q -> q.getCompra().getFechaCorte()).filter(Objects::nonNull)
                .map(YearMonth::from).min(Comparator.naturalOrder()).orElse(current);
        for (var month=first; !month.isAfter(current); month=month.plusMonths(1)) {
            LocalDate period=month.atDay(1);
            Cierre statement=existing.get(period);
            boolean adjustment=statement!=null;
            if (!adjustment) {
                LocalDate day=month.atDay(Math.min(account.getDiaCorte(),month.lengthOfMonth()));
                var cut=account.getHoraCorte()==null ? day.plusDays(1).atStartOfDay() : day.atTime(account.getHoraCorte());
                if (cut.isAfter(now)) continue;
                statement=new Cierre();
                statement.setCuenta(account);
                statement.setPeriodo(period);
                statement.setCorte(cut);
                statement.setVencimiento(FinancialMath.paymentDate(day,account.getDiaCorte(),account.getDiaPago()));
                statement.setMoneda(business.currency(account));
                em.persist(statement);
            }
            var known=new HashSet<>(em.createQuery("select l.cuota.id from LineaCierre l where l.cierre=:c",Integer.class)
                    .setParameter("c",statement).getResultList());
            for(var term:terms) {
                if(known.contains(term.getId())) continue;
                Compra purchase=term.getCompra();
                LocalDateTime at=purchase.getFecha().atTime(Objects.requireNonNullElse(purchase.getHoraCompra(),LocalTime.MIN));
                if(!at.isBefore(statement.getCorte()) || (purchase.getFechaCorte()!=null && YearMonth.from(purchase.getFechaCorte()).isAfter(month))) continue;
                if(Boolean.TRUE.equals(term.getCancelado()) && (term.getFechaPago()==null || term.getFechaPago().isBefore(statement.getCorte().toLocalDate()))) continue;
                var due = new java.sql.Date(term.getFechaVencimiento().getTime()).toLocalDate();
                if(due.isAfter(statement.getVencimiento())) continue;
                var line=new LineaCierre();
                line.setCierre(statement);
                line.setCuota(term);
                line.setAjuste(adjustment);
                line.setCapital(FinancialMath.money(term.getTotal()-term.getIntereses()));
                line.setInteres(FinancialMath.money(term.getIntereses()));
                int days=Math.max(0,FinancialMath.commercialDays(due,statement.getVencimiento()));
                double late=FinancialMath.lateCharge(term.getTotal(),purchase.getTasaMora(),days,purchase.getMoraEfectiva());
                line.setMora(FinancialMath.money(late));
                em.persist(line);
            }
        }
    }

    public record Entry(Integer id, Integer purchaseId, LocalDate purchaseDate, Integer number, BigDecimal capital,
                        BigDecimal interest, BigDecimal late, boolean adjustment) {}
    public record Report(Integer id,String customer,String currency,LocalDate paymentDay,List<Entry> entries) {}
    public List<Report> reports(Integer businessId, YearMonth month) {
        access.business(businessId);
        return em.createQuery("select c from Cierre c where c.cuenta.negocio.id=:b and c.periodo=:p order by c.cuenta.id",Cierre.class)
            .setParameter("b",businessId).setParameter("p",month.atDay(1)).getResultList().stream().map(c -> {
                var entries=em.createQuery("select l from LineaCierre l join fetch l.cuota q join fetch q.compra p where l.cierre=:c order by p.fecha,p.horaCompra,p.id,q.num",LineaCierre.class)
                    .setParameter("c",c).getResultList().stream().map(l -> new Entry(l.getCuota().getId(),l.getCuota().getCompra().getId(),
                        l.getCuota().getCompra().getFecha(),l.getCuota().getNum(),l.getCapital(),l.getInteres(),l.getMora(),l.isAjuste())).toList();
                Cliente client=c.getCuenta().getCliente();
                return new Report(c.getId(),client.getNombre()+" "+client.getApellido(),c.getMoneda(),c.getVencimiento(),entries);
            }).toList();
    }
}
