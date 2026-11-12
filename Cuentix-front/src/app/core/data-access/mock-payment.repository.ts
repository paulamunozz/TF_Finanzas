import { inject, Injectable } from '@angular/core';
import { defer, Observable, of } from 'rxjs';
import { Installment, PurchaseSchedule } from '../models/payment.model';
import { PaymentRepository } from '../repositories/payment.repository';
import { BusinessContext } from '../services/business-context.service';
import { dateKey, isoToday, quoteMora } from '../../financial-engine/domain/schedule-domain';

// Documented cases 1/2 (single payment) and 3 (French schedule).
const purchases: PurchaseSchedule[] = [
  {
    id: 'COMP-001',
    accountId: 'CC-001',
    clientId: 'c1',
    businessId: 'b1',
    currency: 'PEN',
    purchaseDate: '2026-03-10',
    principal: 500,
    graceInterest: 1.15,
    mode: 'single',
    installments: 1,
    products: [{ name: 'Compra comercial · caso 1', quantity: 1, unit: 'operación' }],
  },
  {
    id: 'COMP-002',
    accountId: 'CC-001',
    clientId: 'c1',
    businessId: 'b1',
    currency: 'PEN',
    purchaseDate: '2026-03-06',
    principal: 1200,
    graceInterest: 10.08,
    mode: 'installments',
    installments: 4,
    products: [{ name: 'Compra comercial · caso 3', quantity: 1, unit: 'operación' }],
  },
  {
    id: 'COMP-003',
    accountId: 'CC-002',
    clientId: 'c2',
    businessId: 'b1',
    currency: 'PEN',
    purchaseDate: '2026-03-10',
    principal: 500,
    graceInterest: 1.15,
    mode: 'single',
    installments: 1,
    products: [{ name: 'Compra comercial · caso 2', quantity: 1, unit: 'operación' }],
  },
  {
    id: 'COMP-OTHER',
    accountId: 'CC-OTHER',
    clientId: 'c1',
    businessId: 'b2',
    currency: 'USD',
    purchaseDate: '2026-03-10',
    principal: 500,
    graceInterest: 1.15,
    mode: 'single',
    installments: 1,
    products: [],
  },
];
function seed(): Installment[] {
  const rows: Installment[] = [];
  for (const purchase of purchases) {
    const values =
      purchase.mode === 'single'
        ? [[5.78, 500, 505.78]]
        : [
            [21.89, 294.43, 316.32],
            [16.56, 299.76, 316.32],
            [11.14, 305.18, 316.32],
            [5.62, 310.71, 316.33],
          ];
    values.forEach(([interest, amortization, installmentAmount], index) =>
      rows.push({
        id: `${purchase.id}-${index + 1}`,
        purchaseId: purchase.id,
        businessId: purchase.businessId,
        lateAnnualRate: 36,
        clientId: purchase.clientId,
        purchaseDate: purchase.purchaseDate,
        number: index + 1,
        totalInstallments: values.length,
        dueDate:
          purchase.mode === 'single'
            ? '2026-04-05'
            : `2026-${String(index + 4).padStart(2, '0')}-20`,
        interest,
        amortization,
        installmentAmount,
        lateDays: purchase.id === 'COMP-003' ? 10 : 0,
        lateAmount: purchase.id === 'COMP-003' ? 5.06 : 0,
        cancelled: purchase.id === 'COMP-003',
        paidAt: purchase.id === 'COMP-003' ? '2026-04-15' : undefined,
        products: purchase.products,
      }),
    );
  }
  return rows;
}
@Injectable()
export class MockPaymentRepository implements PaymentRepository {
  private readonly context = inject(BusinessContext);
  private installments = seed();
  private response<T>(operation: () => T): Observable<T> {
    return defer(() => of(structuredClone(operation())));
  }
  getPurchases() {
    return this.response(() => purchases.filter((item) => item.businessId === this.context.id()));
  }
  getPurchase(id: string) {
    return this.response(() => {
      const item = purchases.find((p) => p.id === id && p.businessId === this.context.id());
      if (!item) throw new Error('Compra no encontrada en el negocio activo');
      return item;
    });
  }
  getByClientId(id: string) {
    return this.response(() =>
      this.installments
        .filter((item) => item.clientId === id && item.businessId === this.context.id())
        .map((item) => quoteMora(item, isoToday())),
    );
  }
  getByPurchaseId(id: string) {
    return this.response(() =>
      this.installments
        .filter((item) => item.purchaseId === id && item.businessId === this.context.id())
        .map((item) => quoteMora(item, isoToday())),
    );
  }
  getById(id: string) {
    return this.response(() => {
      const item = this.installments.find((p) => p.id === id && p.businessId === this.context.id());
      if (!item) throw new Error('Cuota no encontrada en el negocio activo');
      return quoteMora(item, isoToday());
    });
  }
  cancel(id: string, paidAt: string) {
    return this.response(() => {
      const item = this.installments.find((p) => p.id === id && p.businessId === this.context.id());
      if (!item) throw new Error('Cuota no encontrada en el negocio activo');
      if (item.cancelled) throw new Error('La cuota ya está cancelada');
      if (dateKey(paidAt) > dateKey(isoToday()) || dateKey(paidAt) < dateKey(item.purchaseDate))
        throw new Error('Fecha efectiva inválida');
      Object.assign(item, quoteMora(item, paidAt), { cancelled: true, paidAt });
      return item;
    });
  }
}
