import { Injectable } from '@angular/core';
import { Observable, defer, of, throwError } from 'rxjs';
import { delay } from 'rxjs/operators';
import { Client, CreateClientDto, UpdateClientDto } from '../models/client.model';
import { ClientRepository } from '../repositories/client.repository';

const initialClients: Client[] = [
  {
    id: 'c1',
    dni: '75535927',
    name: 'Maria Torres',
    currency: 'PEN',
    compensatoryRate: 42,
    rateType: 'TN',
    ratePeriod: 'Mensual',
    lateRate: 12,
    lateRateType: 'TN',
    maxMonths: 6,
    creditLine: 5000,
    cutOffDay: 20,
    paymentDay: 26,
  },
  {
    id: 'c2',
    dni: '67390765',
    name: 'Carlos Ruiz',
    currency: 'PEN',
    compensatoryRate: 36,
    rateType: 'TE',
    ratePeriod: 'Mensual',
    lateRate: 10,
    lateRateType: 'TN',
    maxMonths: 2,
    creditLine: 800,
    cutOffDay: 20,
    paymentDay: 26,
  },
  {
    id: 'c3',
    dni: '72423564',
    name: 'Ana Flores',
    currency: 'PEN',
    compensatoryRate: 36,
    rateType: 'TN',
    ratePeriod: 'Mensual',
    lateRate: 10,
    lateRateType: 'TN',
    maxMonths: 2,
    creditLine: 600,
    cutOffDay: 20,
    paymentDay: 28,
  },
  {
    id: 'c4',
    dni: '69432965',
    name: 'Luis Mendoza',
    currency: 'PEN',
    compensatoryRate: 30,
    rateType: 'TN',
    ratePeriod: 'Mensual',
    lateRate: 8,
    lateRateType: 'TN',
    maxMonths: 1,
    creditLine: 400,
    cutOffDay: 10,
    paymentDay: 15,
  },
];

@Injectable()
export class MockClientRepository implements ClientRepository {
  private clients = initialClients.map((client) => ({ ...client }));
  private response<T>(value: T): Observable<T> {
    return defer(() => of(value)).pipe(delay(150));
  }
  getAll(): Observable<Client[]> {
    return this.response(this.clients.map((client) => ({ ...client })));
  }
  getById(id: string): Observable<Client> {
    const client = this.clients.find((item) => item.id === id);
    return client
      ? this.response({ ...client })
      : throwError(() => new Error('Cliente no encontrado'));
  }
  create(payload: CreateClientDto): Observable<Client> {
    const client = { ...payload, id: `c${Date.now()}` };
    this.clients = [...this.clients, client];
    return this.response({ ...client });
  }
  update(id: string, payload: UpdateClientDto): Observable<Client> {
    if (!this.clients.some((client) => client.id === id))
      return throwError(() => new Error('Cliente no encontrado'));
    const client = { ...payload, id };
    this.clients = this.clients.map((item) => (item.id === id ? client : item));
    return this.response({ ...client });
  }
  delete(id: string): Observable<void> {
    this.clients = this.clients.filter((client) => client.id !== id);
    return this.response(undefined);
  }
}
