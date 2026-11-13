import { Component, inject } from '@angular/core';
import { Router } from '@angular/router';
import { PerfilService } from '../../../core/services/perfil.service';
import { ChangeDetectorRef } from '@angular/core';

@Component({
  selector: 'app-perfil',
  imports: [],
  templateUrl: './perfil.html',
  styleUrl: './perfil.css',
})
export class Perfil {
  private router = inject(Router);
  private perfilService = inject(PerfilService);
  private cdr = inject(ChangeDetectorRef);

  errorCierre = '';
  dni = '';
  nombre = '';
  apellido = '';
  telefono = '';

  constructor() {
    this.cargarDatosSesion();
  }

  get nombreCompleto(): string {
    return `${this.nombre} ${this.apellido}`.trim();
  }

  cargarDatosSesion(): void {
    const sesion = sessionStorage.getItem('cuentix_usuario');

    if (!sesion) {
      return;
    }

    try {
      const usuario = JSON.parse(sesion);

      this.dni = usuario.codigo ?? '';
      this.nombre = usuario.nombre ?? '';
      this.apellido = usuario.apellido ?? '';
      this.telefono = usuario.telefono ?? '';
    } catch {
      this.dni = '';
      this.nombre = '';
      this.apellido = '';
      this.telefono = '';
    }
  }

  cerrarSesion(): void {
    this.errorCierre = '';

    this.perfilService.cerrarSesion().subscribe({
      next: () => {
        sessionStorage.removeItem('cuentix_usuario');
        this.router.navigate(['/login']);
      },

      error: () => {
        this.errorCierre = 'No se pudo cerrar la sesión en el servidor. Intente nuevamente.';

        this.cdr.markForCheck();
      },
    });
  }

  editarDatos(): void {
    this.router.navigate(['/cliente/perfil/editar']);
  }
}
