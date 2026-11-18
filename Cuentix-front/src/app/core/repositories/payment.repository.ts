import { Client } from '../models/client.model';
import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Installment, PurchaseSchedule } from '../models/payment.model';

export interface Statement {client:Client; purchases:PurchaseSchedule[]; items:Installment[]}

export interface PaymentRepository {
  getStatement(id:string): Observable<Statement>;
  getPurchases(): Observable<PurchaseSchedule[]>;
  getPurchase(id: string): Observable<PurchaseSchedule>;
  getByPurchaseId(id: string): Observable<Installment[]>;
  getByClientId(clientId: string): Observable<Installment[]>;
  getById(id: string): Observable<Installment>;
  cancel(id: string, paidAt: string, amount?: number): Observable<Installment>;
}

export const PAYMENT_REPOSITORY = new InjectionToken<PaymentRepository>('PAYMENT_REPOSITORY');
