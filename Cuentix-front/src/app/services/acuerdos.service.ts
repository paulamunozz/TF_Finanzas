
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
  providedIn: 'root'
})
export class AcuerdosService {

  private http = inject(HttpClient);

  obtenerMisAcuerdos(): Observable<AcuerdoClienteDTO[]> {

    const sesion = sessionStorage.getItem(
      'cuentix_usuario'
    );

    let token = '';

    if (sesion) {
      try {
        const usuario = JSON.parse(sesion);
        token = usuario.csrfToken ?? '';
      } catch {
        token = '';
      }
    }

    return this.http.get<AcuerdoClienteDTO[]>(
      '/api/cliente/mis-acuerdos',
      {
        headers: {
          'X-Cuentix-CSRF': token
        }
      }
    );
  }
}
