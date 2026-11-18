import { inject, Injectable } from '@angular/core';
import { PAYMENT_REPOSITORY } from '../repositories/payment.repository';

@Injectable({ providedIn: 'root' })
export class PaymentService {
  private readonly repository = inject(PAYMENT_REPOSITORY);
  readonly getByClientId = (id: string) => this.repository.getByClientId(id);
  readonly getById = (id: string) => this.repository.getById(id);
  readonly cancel = (id: string, paidAt: string) => this.repository.cancel(id, paidAt);
}
