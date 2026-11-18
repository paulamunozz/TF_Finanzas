import { inject, Injectable } from '@angular/core';
import { CLIENT_REPOSITORY } from '../repositories/client.repository';
import { CreateClientDto, UpdateClientDto } from '../models/client.model';

@Injectable({ providedIn: 'root' })
export class ClientService {
  private readonly repository = inject(CLIENT_REPOSITORY);
  readonly getAll = () => this.repository.getAll();
  readonly getById = (id: string) => this.repository.getById(id);
  readonly create = (payload: CreateClientDto) => this.repository.create(payload);
  readonly update = (id: string, payload: UpdateClientDto) => this.repository.update(id, payload);
  readonly delete = (id: string) => this.repository.delete(id);
}
