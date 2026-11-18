import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Installment, PurchaseSchedule } from '../models/payment.model';

export interface PaymentRepository {
  getPurchases(): Observable<PurchaseSchedule[]>;
  getPurchase(id: string): Observable<PurchaseSchedule>;
  getByPurchaseId(id: string): Observable<Installment[]>;
  getByClientId(clientId: string): Observable<Installment[]>;
  getById(id: string): Observable<Installment>;
  cancel(id: string, paidAt: string): Observable<Installment>;
}

export const PAYMENT_REPOSITORY = new InjectionToken<PaymentRepository>('PAYMENT_REPOSITORY');
