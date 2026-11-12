import { API_BASE_URL } from '../config/api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Installment, PurchaseSchedule } from '../models/payment.model';
import { BusinessContext } from '../services/business-context.service';
import { PaymentRepository } from '../repositories/payment.repository';

@Injectable()
export class HttpPaymentRepository implements PaymentRepository {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly baseUrl = `${this.api}/payments`;
  private readonly business = inject(BusinessContext);
  getPurchases(): Observable<PurchaseSchedule[]> {
    return this.http.get<PurchaseSchedule[]>(`${this.api}/purchases`, {
      params: { businessId: this.business.id() },
    });
  }
  getPurchase(id: string): Observable<PurchaseSchedule> {
    return this.http.get<PurchaseSchedule>(`${this.api}/purchases/${encodeURIComponent(id)}`, {
      params: { businessId: this.business.id() },
    });
  }
  getByPurchaseId(id: string): Observable<Installment[]> {
    return this.http.get<Installment[]>(this.baseUrl, {
      params: { businessId: this.business.id(), purchaseId: id },
    });
  }
  getByClientId(clientId: string): Observable<Installment[]> {
    return this.http.get<Installment[]>(this.baseUrl, {
      params: { clientId, businessId: this.business.id() },
    });
  }
  getById(id: string): Observable<Installment> {
    return this.http.get<Installment>(`${this.baseUrl}/${encodeURIComponent(id)}`, {
      params: { businessId: this.business.id() },
    });
  }
  cancel(id: string, paidAt: string): Observable<Installment> {
    return this.http.post<Installment>(`${this.baseUrl}/${encodeURIComponent(id)}/cancel`, {
      paidAt,
      businessId: this.business.id(),
    });
  }
}
