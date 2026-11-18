import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
export interface DatosPerfil {
  nombre: string;
  apellido: string;
  telefono: string;
  contrasena: string;
  contrasenaActual: string;
}
export interface PerfilActualizado {
  id: number;
  dni: string;
  nombre: string;
  apellido: string;
  telefono: string;
  estado: boolean;
}
@Injectable({ providedIn: 'root' })
export class PerfilService {
  private http = inject(HttpClient);
  guardarDatos(datos: DatosPerfil) {
    return this.http.put<PerfilActualizado>('/api/cliente/mi-perfil', datos);
  }
  cerrarSesion() {
    return this.http.post<void>('/api/logout', {});
  }
  eliminarCuenta(contrasenaActual: string) {
    return this.http.delete<void>('/api/cliente/mi-cuenta', { body: { contrasenaActual } });
  }
}
