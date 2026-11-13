import { TestBed } from '@angular/core/testing';
import { firstValueFrom } from 'rxjs';
import { MockPaymentRepository } from '../data-access/mock-payment.repository';
import { BusinessContext } from './business-context.service';
import {
  dateKey,
  installmentStatus,
  quoteMora,
  scheduleSummary,
} from '../../financial-engine/domain/schedule-domain';

describe('Payment schedules', () => {
  let repository: MockPaymentRepository;
  let business: BusinessContext;
  beforeEach(() => {
    TestBed.configureTestingModule({ providers: [MockPaymentRepository] });
    repository = TestBed.inject(MockPaymentRepository);
    business = TestBed.inject(BusinessContext);
    business.id.set('b1');
  });
  it('consolidates multiple purchases without duplicated installments', async () => {
    const items = await firstValueFrom(repository.getByClientId('c1'));
    expect(new Set(items.map((item) => item.purchaseId)).size).toBe(2);
    expect(new Set(items.map((item) => item.id)).size).toBe(items.length);
    expect(items.length).toBe(5);
  });
  it('queries a client with a single purchase', async () => {
    expect((await firstValueFrom(repository.getByClientId('c2'))).length).toBe(1);
  });
  it('returns only the installments belonging to one purchase', async () => {
    const items = await firstValueFrom(repository.getByPurchaseId('COMP-002'));
    expect(items.length).toBe(4);
    expect(items.every((item) => item.purchaseId === 'COMP-002')).toBe(true);
  });
  it('supports a single-payment purchase', async () => {
    expect((await firstValueFrom(repository.getByPurchaseId('COMP-001'))).length).toBe(1);
  });
  it('preserves the documented case 3 scheduled total', async () => {
    const items = await firstValueFrom(repository.getByPurchaseId('COMP-002'));
    expect(scheduleSummary(items, '2026-04-10').scheduled).toBe(1265.29);
    expect(scheduleSummary(items).interest).toBe(55.21);
  });
  it('preserves the documented case 2 mora and full payment', async () => {
    const [item] = await firstValueFrom(repository.getByPurchaseId('COMP-001'));
    expect(quoteMora(item, '2026-04-15').lateAmount).toBe(5.06);
    expect(scheduleSummary([quoteMora(item, '2026-04-15')]).pending).toBe(510.84);
  });
  it('uses the due date and reference to distinguish pending and overdue', async () => {
    const [item] = await firstValueFrom(repository.getByPurchaseId('COMP-001'));
    expect(installmentStatus(item, '2026-04-05')).toBe('pending');
    expect(installmentStatus(item, '2026-04-06')).toBe('overdue');
  });
  it('never marks a paid obligation overdue', async () => {
    const [item] = await firstValueFrom(repository.getByClientId('c2'));
    expect(installmentStatus(item, '2026-11-11')).toBe('paid');
    expect(scheduleSummary([item]).pending).toBe(0);
    expect(scheduleSummary([item]).late).toBe(0);
    expect(scheduleSummary([item]).paid).toBe(510.84);
  });
  it('cancels exactly once and records effective-date mora', async () => {
    const [item] = await firstValueFrom(repository.getByPurchaseId('COMP-001'));
    const paid = await firstValueFrom(repository.cancel(item.id, '2026-04-15'));
    expect(paid.cancelled).toBe(true);
    expect(paid.lateAmount).toBe(5.06);
    await expect(firstValueFrom(repository.cancel(item.id, '2026-04-15'))).rejects.toThrow(
      'ya está cancelada',
    );
  });
  it('rejects nonexistent and cross-business obligations', async () => {
    await expect(firstValueFrom(repository.cancel('missing', '2026-04-15'))).rejects.toThrow();
    await expect(firstValueFrom(repository.cancel('COMP-OTHER-1', '2026-04-15'))).rejects.toThrow();
  });
  it('rejects future dates and dates before the purchase', async () => {
    await expect(firstValueFrom(repository.cancel('COMP-001-1', '2099-01-01'))).rejects.toThrow();
    await expect(firstValueFrom(repository.cancel('COMP-001-1', '2026-02-01'))).rejects.toThrow();
  });
  it('isolates purchase queries and customer schedules by business', async () => {
    expect((await firstValueFrom(repository.getPurchases())).length).toBe(3);
    business.id.set('b2');
    const purchases = await firstValueFrom(repository.getPurchases());
    expect(purchases.map((p) => p.id)).toEqual(['COMP-OTHER']);
    expect((await firstValueFrom(repository.getByClientId('c1'))).length).toBe(1);
    await expect(firstValueFrom(repository.getPurchase('COMP-001'))).rejects.toThrow();
  });
  it('returns defensive copies', async () => {
    const [item] = await firstValueFrom(repository.getByPurchaseId('COMP-001'));
    item.cancelled = true;
    expect((await firstValueFrom(repository.getByPurchaseId('COMP-001')))[0].cancelled).toBe(false);
  });
  it('rejects malformed calendar dates', () => {
    expect(() => dateKey('2026-02-30')).toThrow();
    expect(() => dateKey('11/11/2026')).toThrow();
  });
  it('shows zero balances for an empty schedule', () => {
    expect(scheduleSummary([])).toEqual({
      scheduled: 0,
      paid: 0,
      pending: 0,
      late: 0,
      overdue: 0,
      interest: 0,
    });
  });
});
