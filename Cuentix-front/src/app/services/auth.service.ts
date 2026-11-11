
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';


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

@Injectable({
providedIn: 'root'
})
export class AuthService {

private http = inject(HttpClient);

iniciarSesion(
    codigo: string,
    contrasena: string
): Observable<UsuarioRespuesta> {

    return this.http.post<UsuarioRespuesta>(
    '/login',
    {
        codigo: codigo,
        contrasena: contrasena
    }
    );
}
}
