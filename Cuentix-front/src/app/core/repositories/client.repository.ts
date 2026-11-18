import { InjectionToken } from '@angular/core';
import { Observable } from 'rxjs';
import { Client, CreateClientDto, UpdateClientDto } from '../models/client.model';

export interface ClientRepository {
  getAll(): Observable<Client[]>;
  getById(id: string): Observable<Client>;
  create(payload: CreateClientDto): Observable<Client>;
  update(id: string, payload: UpdateClientDto): Observable<Client>;
  delete(id: string): Observable<void>;
}

export const CLIENT_REPOSITORY = new InjectionToken<ClientRepository>('CLIENT_REPOSITORY');
