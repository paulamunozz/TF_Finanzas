import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { HttpErrorResponse } from '@angular/common/http';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule],
  templateUrl: './login.html',
  styleUrl: './login.css',
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

    // ENVIAR DATOS AL BACKEND
    this.cargando = true;

    this.authService.iniciarSesion(documento, this.contrasena).subscribe({
      next: (usuario) => {
        this.cargando = false;
        this.error = '';

        this.router.navigate([usuario.rolId === 1 ? '/admin/negocios' : usuario.rolId === 2 ? '/cliente/reporte' : '/dashboard']);

        // Notificar a Angular que debe actualizar la pantalla
        this.cdr.markForCheck();
      },

      error: (err: HttpErrorResponse) => {
        this.cargando = false;
        this.aviso = '';

        if (err.error?.message === 'Código o contraseña incorrectos') {
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
      },
    });
  }

  mostrarRegistro(): void {
    this.error = '';
    this.aviso = '';

    this.router.navigate(['/registro']);
  }
}
