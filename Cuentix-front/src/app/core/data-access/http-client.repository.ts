import { API_BASE_URL } from '../config/api.config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Client, CreateClientDto, UpdateClientDto } from '../models/client.model';
import { ClientRepository } from '../repositories/client.repository';

@Injectable()
export class HttpClientRepository implements ClientRepository {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly baseUrl = `${this.api}/clients`;
  getAll(): Observable<Client[]> {
    return this.http.get<Client[]>(this.baseUrl);
  }
  getById(id: string): Observable<Client> {
    return this.http.get<Client>(`${this.baseUrl}/${id}`);
  }
  create(payload: CreateClientDto): Observable<Client> {
    return this.http.post<Client>(this.baseUrl, payload);
  }
  update(id: string, payload: UpdateClientDto): Observable<Client> {
    return this.http.put<Client>(`${this.baseUrl}/${id}`, payload);
  }
  delete(id: string): Observable<void> {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
