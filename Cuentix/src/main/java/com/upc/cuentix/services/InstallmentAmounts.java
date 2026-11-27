package com.upc.cuentix.services;

import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.Cuota;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

record InstallmentAmounts(BigDecimal capital, BigDecimal interest, BigDecimal graceInterest) {
  private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2);

  static LocalDate dueDate(Cuota term) {
    return new java.sql.Date(term.getFechaVencimiento().getTime()).toLocalDate();
  }

  static BigDecimal lateAt(Cuota term, LocalDate date) {
    Compra purchase = term.getCompra();
    int days = Math.max(0, FinancialMath.commercialDays(dueDate(term), date));
    return FinancialMath.money(
        FinancialMath.lateCharge(term.getTotal(), purchase.getTasaMora(), days, purchase.getMoraEfectiva()));
  }

  static Map<Integer, InstallmentAmounts> byTerm(List<Cuota> terms) {
    Map<Integer, InstallmentAmounts> result = new HashMap<>();
    var schedules = terms.stream().collect(Collectors.groupingBy(term -> term.getCompra().getId()));
    for (var schedule : schedules.values()) {
      allocateGrace(schedule, result);
    }
    return result;
  }

  private static void allocateGrace(List<Cuota> schedule, Map<Integer, InstallmentAmounts> result) {
    schedule.sort(Comparator.comparing(Cuota::getNum));
    Compra purchase = schedule.get(0).getCompra();
    BigDecimal amortized = schedule.stream().map(InstallmentAmounts::amortization)
        .reduce(ZERO, BigDecimal::add);
    BigDecimal financedGrace = ZERO;
    if ("installments".equals(purchase.getModalidad()) && purchase.getTotalGracia() != null
        && purchase.getTotalInicial() != null && amortized.signum() > 0) {
      financedGrace = FinancialMath.money(purchase.getTotalGracia())
          .subtract(FinancialMath.money(purchase.getTotalInicial())).max(ZERO).min(amortized);
    }

    BigDecimal cumulativeAmortization = ZERO;
    BigDecimal allocatedGrace = ZERO;
    for (Cuota term : schedule) {
      BigDecimal interest = FinancialMath.money(term.getIntereses());
      BigDecimal capital = amortization(term);
      if ("single".equals(purchase.getModalidad())) {
        result.put(term.getId(), new InstallmentAmounts(capital, interest, interest));
        continue;
      }
      cumulativeAmortization = cumulativeAmortization.add(capital);
      BigDecimal cumulativeGrace = financedGrace.signum() == 0 ? ZERO
          : financedGrace.multiply(cumulativeAmortization).divide(amortized, 2, RoundingMode.HALF_UP);
      BigDecimal grace = cumulativeGrace.subtract(allocatedGrace);
      result.put(term.getId(), new InstallmentAmounts(capital.subtract(grace), interest.add(grace), grace));
      allocatedGrace = cumulativeGrace;
    }
  }

  private static BigDecimal amortization(Cuota term) {
    return FinancialMath.money(term.getTotal()).subtract(FinancialMath.money(term.getIntereses()));
  }
}
