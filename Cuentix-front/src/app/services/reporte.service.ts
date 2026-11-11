
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface ClienteDTO {
  id: number;
  dni: string;
  nombre: string;
  apellido: string;
  telefono: string;
  estado: boolean;
}

export interface CompraDTO {
  id: number;
  ccId: number | null;
  fecha: string | null;
  totalFinal: number | null;
  moras: number | null;
  numMeses: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class ReporteService {

  private http = inject(HttpClient);

  buscarClientePorDni(dni: string): Observable<ClienteDTO> {
    return this.http.post<ClienteDTO>(
      '/buscarClientePorDNI',
      dni,
      {
        headers: {
          'Content-Type': 'text/plain'
        }
      }
    );
  }

  comprasPorCliente(idCliente: number): Observable<CompraDTO[]> {
    return this.http.post<CompraDTO[]>(
      '/comprasPorCliente',
      idCliente
    );
  }
}
