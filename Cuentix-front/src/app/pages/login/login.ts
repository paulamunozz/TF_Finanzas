import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import {
  AuthService
} from '../../services/auth.service'; 

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css'
})
export class Login {

  private authService = inject(AuthService);
  private cdr = inject(ChangeDetectorRef);
  private router = inject(Router);

  codigo: string = '';
  contrasena: string = '';

  error: string = '';
  aviso: string = '';
  cargando: boolean = false;

  // VALIDAR RUC
  validarRUC(ruc: string): boolean {

    if (!/^\d{11}$/.test(ruc)) {
      return false;
    }

    const pesos = [5, 4, 3, 2, 7, 6, 5, 4, 3, 2];
    let suma = 0;

    for (let i = 0; i < 10; i++) {
      suma += Number(ruc[i]) * pesos[i];
    }

    const digitoCalculado = (11 - (suma % 11)) % 10;

    return digitoCalculado === Number(ruc[10]);
  }

  // INICIAR SESIÓN
  iniciarSesion(): void {

    if (this.cargando) {
      return;
    }

    this.error = '';
    this.aviso = '';

    const documento = this.codigo.trim();

    // Validar campos vacíos
    if (!documento || !this.contrasena) {
      this.error = 'Complete todos los campos.';
      return;
    }

    // Validar que solo tenga números
    if (!/^\d+$/.test(documento)) {
      this.error = 'El DNI o RUC solo debe contener números.';
      return;
    }

    // Validar longitud del documento
    if (documento.length !== 8 && documento.length !== 11) {
      this.error =
        'El DNI debe tener 8 dígitos y el RUC 11 dígitos.';
      return;
    }

    // Validar dígito verificador del RUC
    if (documento.length === 11 && !this.validarRUC(documento)) {
      this.error = 'El número de RUC ingresado no es válido.';
      return;
    }

    // ENVIAR DATOS AL BACKEND
    this.cargando = true;

    
this.authService.iniciarSesion(
  documento,
  this.contrasena
).subscribe({

  next: (usuario) => {

    this.cargando = false;
    this.error = '';


sessionStorage.setItem(
  'cuentix_usuario',
  JSON.stringify({
    id: usuario.id,
    codigo: usuario.codigo,
    rolId: usuario.rolId,
    nombre: usuario.nombre ?? '',
    apellido: usuario.apellido ?? '',
    telefono: usuario.telefono ?? '',
    csrfToken: usuario.csrfToken
  })
);

    this.router.navigate(['/cliente/reporte']);

    // Notificar a Angular que debe actualizar la pantalla
    this.cdr.markForCheck();
  },

  error: (err: HttpErrorResponse) => {

    this.cargando = false;
    this.aviso = '';

    if (
      err.error?.message ===
      'Código o contraseña incorrectos'
    ) {
      this.error = 'Los datos ingresados son incorrectos';

    } else if (
      err.status === 0 ||
      err.status === 502 ||
      err.status === 503 ||
      err.status === 504
    ) {
      this.error = 'No se pudo conectar con el servidor.';

    } else {
      this.error = 'No fue posible iniciar sesión.';
    }

    // Actualizar también cuando ocurra un error
    this.cdr.markForCheck();
  }
});
  }

  
mostrarRegistro(): void {
  this.error = '';
  this.aviso = '';

  this.router.navigate(['/registro']);
}
}
