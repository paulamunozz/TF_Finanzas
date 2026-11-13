import { AUTH_PREVIEW } from '../../../core/config/auth-preview.config';
import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';

import { ChangeDetectorRef } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { PerfilService } from '../../../core/services/perfil.service';

@Component({
  selector: 'app-editar-perfil',
  imports: [FormsModule],
  templateUrl: './editar-perfil.html',
  styleUrl: './editar-perfil.css',
})
export class EditarPerfil {
  private readonly preview = inject(AUTH_PREVIEW);
  private router = inject(Router);
  private perfilService = inject(PerfilService);
  private cdr = inject(ChangeDetectorRef);

  contrasenaActual = '';
  guardando = false;

  contrasenaEliminar = '';
  eliminando = false;

  dni = '';
  nombre = '';
  apellido = '';
  telefono = '';

  nuevaContrasena = '';
  confirmarContrasena = '';

  error = '';
  aviso = '';

  confirmarEliminacion = false;

  constructor() {
    this.cargarDatos();
  }

  cargarDatos(): void {
    const sesion = sessionStorage.getItem('cuentix_usuario');

    if (!sesion) {
      if (!this.preview) this.router.navigate(['/login']);
      return;
    }

    try {
      const usuario = JSON.parse(sesion);

      this.dni = usuario.codigo ?? '';
      this.nombre = usuario.nombre ?? '';
      this.apellido = usuario.apellido ?? '';
      this.telefono = usuario.telefono ?? '';
    } catch {
      if (!this.preview) this.router.navigate(['/login']);
    }
  }

  guardarDatos(): void {
    if (this.guardando) return;

    this.error = '';
    this.aviso = '';

    const nombre = this.nombre.trim();
    const apellido = this.apellido.trim();
    const telefono = this.telefono.trim();

    if (!nombre || !apellido || !telefono) {
      this.error = 'Complete todos los campos obligatorios.';
      return;
    }

    if (!/^[\p{L} .'-]+$/u.test(nombre) || !/^[\p{L} .'-]+$/u.test(apellido)) {
      this.error = 'Nombre o apellido inválido.';
      return;
    }

    if (!/^9\d{8}$/.test(telefono)) {
      this.error = 'Ingrese un teléfono válido de 9 dígitos.';
      return;
    }

    if (this.nuevaContrasena || this.confirmarContrasena) {
      if (this.nuevaContrasena.length < 8) {
        this.error = 'La contraseña debe tener al menos 8 caracteres.';
        return;
      }

      if (this.nuevaContrasena !== this.confirmarContrasena) {
        this.error = 'Las contraseñas no coinciden.';
        return;
      }

      if (!this.contrasenaActual) {
        this.error = 'Ingrese su contraseña actual.';
        return;
      }
    }

    this.guardando = true;

    this.perfilService
      .guardarDatos({
        nombre,
        apellido,
        telefono,
        contrasena: this.nuevaContrasena,
        contrasenaActual: this.contrasenaActual,
      })
      .subscribe({
        next: (respuesta) => {
          this.guardando = false;

          const sesion = JSON.parse(sessionStorage.getItem('cuentix_usuario') || '{}');

          // Actualizar los datos locales sin guardar contraseñas.
          sessionStorage.setItem(
            'cuentix_usuario',
            JSON.stringify({
              ...sesion,
              nombre: respuesta.nombre,
              apellido: respuesta.apellido,
              telefono: respuesta.telefono,
            }),
          );

          this.nuevaContrasena = '';
          this.confirmarContrasena = '';
          this.contrasenaActual = '';

          // Volver a Mi perfil con los datos actualizados.
          this.router.navigate(['/cliente/perfil']);
        },

        error: (err: HttpErrorResponse) => {
          this.guardando = false;

          if (err.status === 401) {
            this.error = 'La sesión expiró. Inicie sesión nuevamente.';
          } else if (err.status === 403) {
            this.error = 'No tiene autorización para modificar el perfil.';
          } else {
            this.error = err.error?.message || 'No se pudieron guardar los cambios.';
          }

          this.cdr.markForCheck();
        },
      });
  }

  eliminarCuenta(): void {
    this.confirmarEliminacion = true;
    this.error = '';
    this.aviso = '';
  }

  cancelarEliminacion(): void {
    this.confirmarEliminacion = false;
    this.contrasenaEliminar = '';
    this.error = '';
  }

  confirmarEliminarCuenta(): void {
    if (this.eliminando) return;

    this.error = '';
    this.aviso = '';

    if (!this.contrasenaEliminar.trim()) {
      this.error = 'Ingrese su contraseña actual para eliminar la cuenta.';
      return;
    }

    this.eliminando = true;

    this.perfilService.eliminarCuenta(this.contrasenaEliminar).subscribe({
      next: () => {
        this.eliminando = false;
        this.contrasenaEliminar = '';

        // Eliminar los datos locales de sesión.
        sessionStorage.removeItem('cuentix_usuario');

        // Regresar al inicio de sesión.
        if (!this.preview) this.router.navigate(['/login']);
      },

      error: (err: HttpErrorResponse) => {
        this.eliminando = false;

        if (err.status === 400) {
          this.error = err.error?.message || 'La contraseña ingresada es incorrecta.';
        } else if (err.status === 401) {
          this.error = 'La sesión expiró. Inicie sesión nuevamente.';
        } else if (err.status === 403) {
          this.error = 'No tiene permiso para eliminar esta cuenta.';
        } else {
          this.error = 'No se pudo eliminar la cuenta. Inténtelo nuevamente.';
        }

        this.cdr.markForCheck();
      },
    });
  }

  volverPerfil(): void {
    this.router.navigate(['/cliente/perfil']);
  }
}
