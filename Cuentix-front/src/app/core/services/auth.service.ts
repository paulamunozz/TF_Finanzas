import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap } from 'rxjs';
export interface UsuarioRespuesta {
  id: number;
  codigo: string;
  rolId: number | null;
  estado: boolean;
  nombre: string | null;
  apellido: string | null;
  telefono: string | null;
  csrfToken: string;
}
@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  iniciarSesion(codigo: string, contrasena: string) {
    return this.http
      .post<UsuarioRespuesta>('/api/auth/login', { codigo, contrasena })
      .pipe(tap((u) => this.save(u)));
  }
  recuperar() {
    return this.http.get<UsuarioRespuesta>('/api/auth/session').pipe(tap((u) => this.save(u)));
  }
  save(u: UsuarioRespuesta) {
    sessionStorage.setItem('cuentix_usuario', JSON.stringify(u));
  }
  logout() {
    return this.http
      .post<void>('/api/logout', {})
      .pipe(tap(() => sessionStorage.removeItem('cuentix_usuario')));
  }
}
