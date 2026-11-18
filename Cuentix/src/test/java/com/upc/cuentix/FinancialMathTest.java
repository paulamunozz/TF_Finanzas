package com.upc.cuentix;

import static org.junit.jupiter.api.Assertions.*;

import com.upc.cuentix.entities.Tasa;
import com.upc.cuentix.services.FinancialMath;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class FinancialMathTest {
  private Tasa rate(boolean effective, double percentage, double period, double cap) {
    Tasa t = new Tasa();
    t.setNominalEfectiva(effective);
    t.setPorcentaje(percentage);
    t.setPeriodicidad(period);
    t.setCapitalizacion(cap);
    return t;
  }

  @Test
  void nominalAnnualMonthly() {
    assertEquals(Math.pow(1.01, 12) - 1, FinancialMath.annual(rate(false, .12, 360, 30)), 1e-12);
  }

  @Test
  void effectiveMonthly() {
    assertEquals(Math.pow(1.02, 12) - 1, FinancialMath.annual(rate(true, .02, 30, 30)), 1e-12);
  }

  @Test
  void effectiveAnnualUnchanged() {
    assertEquals(.2, FinancialMath.annual(rate(true, .2, 360, 30)), 1e-12);
  }

  @Test
  void appliesCapitalAndFractionalYears() {
    assertEquals(1100, FinancialMath.compound(1000, .21, 180));
    assertEquals(1210, FinancialMath.compound(1000, .21, 360));
  }

  @Test
  void zeroRateAndZeroDays() {
    assertEquals(1000, FinancialMath.compound(1000, 0, 180));
    assertEquals(1000, FinancialMath.compound(1000, .2, 0));
  }

  @Test
  void rejectsInvalidPeriods() {
    assertThrows(
        IllegalArgumentException.class, () -> FinancialMath.annual(rate(false, .12, 0, 30)));
    assertThrows(IllegalArgumentException.class, () -> FinancialMath.compound(1000, .2, -1));
  }

  @Test
  void clampsFebruaryAndMovesPaymentPastCutoff() {
    assertEquals(
        LocalDate.of(2025, 2, 28), FinancialMath.paymentDate(LocalDate.of(2025, 2, 20), 25, 31));
    assertEquals(
        LocalDate.of(2025, 3, 5), FinancialMath.paymentDate(LocalDate.of(2025, 2, 20), 25, 5));
  }

  @Test
  void frenchPrincipalIsFullyAmortized() {
    var terms = FinancialMath.french(1000, .21, 12);
    assertEquals(
        new java.math.BigDecimal("1000.00"),
        terms.stream()
            .map(FinancialMath.Term::amortization)
            .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add));
    assertTrue(terms.get(0).interest().compareTo(terms.get(11).interest()) > 0);
    assertEquals(
        new java.math.BigDecimal("250.00"), FinancialMath.french(1000, 0, 4).get(0).amount());
  }

  @Test
  void simpleAnnualMoraHasSameCentsAsFrontend() {
    assertEquals(.5, FinancialMath.lateAmount(500, 36, 1));
    assertEquals(15, FinancialMath.lateAmount(500, 36, 30));
    assertEquals(0, FinancialMath.lateAmount(500, 36, 0));
  }

  @Test
  void moneyRoundsHalfUp() {
    assertEquals("10.01", FinancialMath.money(10.005).toPlainString());
  }

  @Test
  void septemberCreditAndCutoffBoundary() {
    var date = LocalDate.of(2026, 9, 15);
    var close = FinancialMath.cutoffDate(date.atTime(12,0),20,java.time.LocalTime.of(18,0));
    var graceEnd = FinancialMath.paymentDate(close,20,26);
    assertEquals(11, FinancialMath.commercialDays(date,graceEnd));
    double capital = FinancialMath.compound(1000,.24,11);
    assertEquals(1006.59,capital);
    var schedule = FinancialMath.french(capital,.24,3);
    assertEquals(java.util.List.of("347.74","347.74","347.75"),
        schedule.stream().map(t -> t.amount().toPlainString()).toList());
    assertEquals(LocalDate.of(2026,12,26),FinancialMath.monthlyDue(graceEnd,3,26));
    var atClose = FinancialMath.cutoffDate(close.atTime(18,0),20,java.time.LocalTime.of(18,0));
    assertEquals(LocalDate.of(2026,11,5),FinancialMath.paymentDate(atClose,20,5));
    assertEquals(close,FinancialMath.cutoffDate(close.atTime(17,59,59),20,java.time.LocalTime.of(18,0)));
    assertEquals(5.06,FinancialMath.lateCharge(505.78,.36,10,false));
    assertEquals(10.0,FinancialMath.lateCharge(100,.21,180,true));
  }
}
