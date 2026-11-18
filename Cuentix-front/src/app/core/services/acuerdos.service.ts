import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface AcuerdoClienteDTO {
  id: number;
  tienda: string;
  tasa: number | null;
  tipoTasa: string;
  linea: number;
  moneda: string;
  meses: number;
  diaCorte: number;
  diaPago: number;
}

@Injectable({
  providedIn: 'root',
})
export class AcuerdosService {
  private http = inject(HttpClient);

  obtenerMisAcuerdos(): Observable<AcuerdoClienteDTO[]> {
    return this.http.get<AcuerdoClienteDTO[]>('/api/cliente/mis-acuerdos');
  }
}
