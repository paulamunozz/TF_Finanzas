import { API_BASE_URL } from '../config/api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Installment, PurchaseSchedule } from '../models/payment.model';
import { PaymentRepository } from '../repositories/payment.repository';

@Injectable()
export class HttpPaymentRepository implements PaymentRepository {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly baseUrl = `${this.api}/payments`;
  getPurchases(): Observable<PurchaseSchedule[]> {
    return this.http.get<PurchaseSchedule[]>(`${this.api}/purchases`);
  }
  getPurchase(id: string): Observable<PurchaseSchedule> {
    return this.http.get<PurchaseSchedule>(`${this.api}/purchases/${encodeURIComponent(id)}`);
  }
  getByPurchaseId(id: string): Observable<Installment[]> {
    return this.http.get<Installment[]>(this.baseUrl, {
      params: { purchaseId: id },
    });
  }
  getByClientId(clientId: string): Observable<Installment[]> {
    return this.http.get<Installment[]>(this.baseUrl, {
      params: { clientId },
    });
  }
  getById(id: string): Observable<Installment> {
    return this.http.get<Installment>(`${this.baseUrl}/${encodeURIComponent(id)}`);
  }
  cancel(id: string, paidAt: string): Observable<Installment> {
    return this.http.post<Installment>(`${this.baseUrl}/${encodeURIComponent(id)}/cancel`, {
      paidAt,
    });
  }
}
