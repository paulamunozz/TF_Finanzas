package com.upc.cuentix.services;

import com.upc.cuentix.entities.Tasa;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;

public final class FinancialMath {

  private FinancialMath() {}

  public static double lateAmount(double amount, double annualPercent, int days) {
    if (amount < 0
        || annualPercent < 0
        || days < 0
        || !Double.isFinite(amount)
        || !Double.isFinite(annualPercent)) throw new IllegalArgumentException("Mora invalida");
    return BigDecimal.valueOf(amount)
        .setScale(2, RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(annualPercent).setScale(7, RoundingMode.HALF_UP))
        .multiply(BigDecimal.valueOf(days))
        .divide(BigDecimal.valueOf(36000), 2, RoundingMode.HALF_UP)
        .doubleValue();
  }

  public record Term(BigDecimal amount, BigDecimal interest, BigDecimal amortization) {}

  public static java.util.List<Term> french(double capital, double tea, int months) {
    if (months < 1 || months > 120 || capital <= 0 || tea < 0)
      throw new IllegalArgumentException("Financiacion invalida");
    double monthly = Math.pow(1 + tea, 1.0 / 12) - 1;
    BigDecimal balance = money(capital);
    BigDecimal fixed =
        money(
            monthly == 0
                ? capital / months
                : capital * monthly / (1 - Math.pow(1 + monthly, -months)));
    java.util.List<Term> result = new java.util.ArrayList<>();
    for (int n = 1; n <= months; n++) {
      BigDecimal interest = money(balance.doubleValue() * monthly);
      BigDecimal amortization = n == months ? balance : fixed.subtract(interest).min(balance);
      if (amortization.signum() < 0) throw new IllegalArgumentException("Amortizacion negativa");
      result.add(new Term(amortization.add(interest), interest, amortization));
      balance = balance.subtract(amortization);
    }
    return result;
  }

  public static double annual(Tasa t) {
    if (t == null
        || t.getPorcentaje() == null
        || t.getPeriodicidad() == null
        || t.getCapitalizacion() == null
        || t.getNominalEfectiva() == null
        || !Double.isFinite(t.getPorcentaje())
        || t.getPorcentaje() < 0
        || !Double.isFinite(t.getPeriodicidad())
        || t.getPeriodicidad() <= 0
        || !Double.isFinite(t.getCapitalizacion())
        || t.getCapitalizacion() <= 0) throw new IllegalArgumentException("Tasa invalida");
    double rate =
        t.getNominalEfectiva()
            ? Math.pow(1 + t.getPorcentaje(), 360.0 / t.getPeriodicidad()) - 1
            : Math.pow(
                    1 + t.getPorcentaje() / (t.getPeriodicidad() / t.getCapitalizacion()),
                    360.0 / t.getCapitalizacion())
                - 1;
    if (!Double.isFinite(rate)) throw new IllegalArgumentException("Tasa fuera de rango");
    return rate;
  }

  public static BigDecimal money(double amount) {
    if (!Double.isFinite(amount)) throw new IllegalArgumentException("Monto invalido");
    return BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP);
  }

  public static double compound(double principal, double tea, long days) {
    if (principal < 0 || tea < 0 || days < 0)
      throw new IllegalArgumentException("Periodo o capital invalido");
    return money(
            BigDecimal.valueOf(principal)
                .multiply(BigDecimal.valueOf(Math.pow(1 + tea, days / 360.0)))
                .doubleValue())
        .doubleValue();
  }

  private static LocalDate day(YearMonth month, int day) {
    if (day < 1 || day > 31) throw new IllegalArgumentException("Dia invalido");
    return month.atDay(Math.min(day, month.lengthOfMonth()));
  }

  public static LocalDate paymentDate(LocalDate purchase, int cutoff, int payment) {
    YearMonth m = YearMonth.from(purchase);
    if (purchase.isAfter(day(m, cutoff))) m = m.plusMonths(1);
    LocalDate due = day(m, payment);
    if (payment < cutoff || due.isBefore(purchase)) due = day(m.plusMonths(1), payment);
    return due;
  }
}
