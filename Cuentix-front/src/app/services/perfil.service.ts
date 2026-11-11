
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

@Injectable({
  providedIn: 'root'
})
export class PerfilService {

  private http = inject(HttpClient);

  private obtenerToken(): string {
    const sesion = sessionStorage.getItem('cuentix_usuario');

    if (!sesion) {
      return '';
    }

    try {
      return JSON.parse(sesion).csrfToken ?? '';
    } catch {
      return '';
    }
  }

  guardarDatos(datos: DatosPerfil) {
    return this.http.put<PerfilActualizado>(
      '/api/cliente/mi-perfil',
      datos,
      {
        headers: {
          'X-Cuentix-CSRF': this.obtenerToken()
        }
      }
    );
  }

  cerrarSesion() {
    return this.http.post<void>(
      '/api/logout',
      {},
      {
        headers: {
          'X-Cuentix-CSRF': this.obtenerToken()
        }
      }
    );
  }

eliminarCuenta(contrasenaActual: string) {

  return this.http.delete<void>(
    '/api/cliente/mi-cuenta',
    {
      body: {
        contrasenaActual
      },
      headers: {
        'X-Cuentix-CSRF': this.obtenerToken()
      }
    }
  );
}
}
