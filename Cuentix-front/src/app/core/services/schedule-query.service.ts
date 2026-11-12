import { inject, Injectable } from '@angular/core';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { PAYMENT_REPOSITORY } from '../repositories/payment.repository';
import { ClientService } from './client.service';
import { scheduleSummary } from '../../financial-engine/domain/schedule-domain';
@Injectable({ providedIn: 'root' })
export class ScheduleQuery {
  readonly repository = inject(PAYMENT_REPOSITORY);
  private readonly clients = inject(ClientService);
  customer(id: string) {
    return forkJoin({
      client: this.clients.getById(id),
      items: this.repository.getByClientId(id),
      purchases: this.repository.getPurchases(),
    }).pipe(
      map((state) => {
        const purchases = state.purchases.filter((p) => p.clientId === id);
        if (new Set(purchases.map((p) => p.currency)).size > 1)
          throw new Error(
            'La consulta contiene varias monedas. Consulta el cronograma individual de cada compra.',
          );
        if (
          state.items.some(
            (item) =>
              !purchases.some((p) => p.id === item.purchaseId && p.businessId === item.businessId),
          )
        )
          throw new Error('Una cuota no tiene una compra válida en esta cuenta.');
        return {
          ...state,
          client: { ...state.client, currency: purchases[0]?.currency ?? state.client.currency },
          purchases,
          summary: scheduleSummary(state.items),
        };
      }),
    );
  }
  purchase(id: string) {
    return this.repository.getPurchase(id).pipe(
      switchMap((purchase) =>
        forkJoin({
          client: this.clients.getById(purchase.clientId),
          items: this.repository.getByPurchaseId(id),
          purchases: of([purchase]),
        }),
      ),
      map((state) => ({
        ...state,
        client: { ...state.client, currency: state.purchases[0].currency },
        summary: scheduleSummary(state.items),
      })),
    );
  }
}
