import { Installment } from '../../core/models/payment.model';
export type InstallmentStatus = 'pending' | 'overdue' | 'paid';
export function isoToday(): string {
  return new Intl.DateTimeFormat('en-CA', {
    timeZone: 'America/Lima',
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).format(new Date());
}
export function dateKey(value: string): number {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) throw new Error('Fecha inválida');
  const [year, month, day] = value.split('-').map(Number);
  const date = new Date(Date.UTC(year, month - 1, day));
  if (
    date.getUTCFullYear() !== year ||
    date.getUTCMonth() !== month - 1 ||
    date.getUTCDate() !== day
  )
    throw new Error('Fecha inválida');
  return date.getTime();
}
export function installmentStatus(item: Installment, reference = isoToday()): InstallmentStatus {
  return item.cancelled
    ? 'paid'
    : dateKey(item.dueDate) < dateKey(reference)
      ? 'overdue'
      : 'pending';
}
export const statusLabels: Record<InstallmentStatus, string> = {
  pending: 'Pendiente',
  overdue: 'Vencida',
  paid: 'Cancelada',
};
export function sumMoney(values: number[]): number {
  return values.reduce((sum, value) => sum + Math.round(value * 100), 0) / 100;
}
/** Documented nominal annual mora, non-capitalizable, commercial 30/360. */
export function quoteMora(item: Installment, reference: string): Installment {
  dateKey(reference);
  dateKey(item.dueDate);
  if (item.cancelled) return { ...item };
  const [y, m, d] = reference.split('-').map(Number);
  const [dy, dm, dd] = item.dueDate.split('-').map(Number);
  const lateDays = Math.max(0, (y - dy) * 360 + (m - dm) * 30 + Math.min(d, 30) - Math.min(dd, 30));
  // Integer arithmetic preserves cents and the rate's documented seven decimals.
  const numerator =
    BigInt(Math.round(item.installmentAmount * 100)) *
    BigInt(Math.round(item.lateAnnualRate * 1e7)) *
    BigInt(lateDays);
  const denominator = 360n * 100n * 10000000n;
  const lateAmount = Number((numerator + denominator / 2n) / denominator) / 100;
  return { ...item, lateDays, lateAmount };
}
export function scheduleSummary(items: Installment[], reference = isoToday()) {
  const unpaid = items.filter((item) => !item.cancelled);
  return {
    scheduled: sumMoney(items.map((item) => item.installmentAmount)),
    paid: sumMoney(
      items
        .filter((item) => item.cancelled)
        .map((item) => item.installmentAmount + item.lateAmount),
    ),
    pending: sumMoney(unpaid.map((item) => item.installmentAmount + item.lateAmount)),
    late: sumMoney(unpaid.map((item) => item.lateAmount)),
    overdue: unpaid.filter((item) => installmentStatus(item, reference) === 'overdue').length,
    interest: sumMoney(items.map((item) => item.interest)),
  };
}
