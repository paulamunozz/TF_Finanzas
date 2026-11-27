package com.upc.cuentix.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.upc.cuentix.entities.Compra;
import com.upc.cuentix.entities.Cuota;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class InstallmentAmountsTest {
  @Test
  void apportionsGraceWithoutChangingTheAmountOfAnyInstallment() {
    Compra purchase = new Compra();
    purchase.setId(1);
    purchase.setModalidad("installments");
    purchase.setTotalInicial(1000.0);
    purchase.setTotalGracia(1006.59);
    List<Cuota> installments = new ArrayList<>();
    for (var generated : FinancialMath.french(1006.59, .24, 3)) {
      Cuota term = new Cuota();
      term.setId(installments.size() + 1);
      term.setNum(term.getId());
      term.setCompra(purchase);
      term.setIntereses(generated.interest().doubleValue());
      term.setTotal(generated.amount().doubleValue());
      installments.add(term);
    }
    Collections.reverse(installments);

    var amounts = InstallmentAmounts.byTerm(installments);
    for (Cuota term : installments) {
      var split = amounts.get(term.getId());
      assertEquals(FinancialMath.money(term.getTotal()), split.capital().add(split.interest()));
    }
    assertEquals(new BigDecimal("1000.00"), amounts.values().stream().map(InstallmentAmounts::capital)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    assertEquals(new BigDecimal("43.23"), amounts.values().stream().map(InstallmentAmounts::interest)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
    assertEquals(new BigDecimal("6.59"), amounts.values().stream().map(InstallmentAmounts::graceInterest)
        .reduce(BigDecimal.ZERO, BigDecimal::add));
  }
}
