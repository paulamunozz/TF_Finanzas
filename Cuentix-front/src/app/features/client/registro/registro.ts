import { Component, inject, ChangeDetectorRef } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-registro',
  imports: [FormsModule, RouterLink],
  templateUrl: './registro.html',
  styleUrl: './registro.css',
})
export class Registro {
  private http = inject(HttpClient);
  private cdr = inject(ChangeDetectorRef);

  dni = '';
  nombre = '';
  apellido = '';
  contrasena = '';
  confirmarContrasena = '';
  telefono = '';

  error = '';
  registrado = false;
  cargando = false;

  registrarCliente(): void {
    if (this.cargando) return;

    this.error = '';

    // Validar campos obligatorios
    if (
      !this.dni.trim() ||
      !this.nombre.trim() ||
      !this.apellido.trim() ||
      !this.contrasena ||
      !this.confirmarContrasena ||
      !this.telefono.trim()
    ) {
      this.error = 'Complete todos los campos.';
      return;
    }

    // Validar DNI
    if (!/^\d{8}$/.test(this.dni.trim())) {
      this.error = 'El DNI debe tener exactamente 8 dígitos.';
      return;
    }

    // Validar nombre y apellido
    if (
      !/^[\p{L}\s'-]+$/u.test(this.nombre.trim()) ||
      !/^[\p{L}\s'-]+$/u.test(this.apellido.trim())
    ) {
      this.error = 'El nombre y apellido contienen caracteres no válidos.';
      return;
    }

    // Validar contraseña
    if (this.contrasena.length < 8) {
      this.error = 'La contraseña debe tener al menos 8 caracteres.';
      return;
    }

    // Confirmar contraseña
    if (this.contrasena !== this.confirmarContrasena) {
      this.error = 'Las contraseñas no coinciden.';
      return;
    }

    // Validar celular peruano
    if (!/^9\d{8}$/.test(this.telefono.trim())) {
      this.error = 'Ingrese un celular válido de 9 dígitos.';
      return;
    }

    // Enviar los datos al backend
    this.cargando = true;

    const cliente = {
      dni: this.dni.trim(),
      nombre: this.nombre.trim(),
      apellido: this.apellido.trim(),
      contrasena: this.contrasena,
      telefono: this.telefono.trim(),
    };

    this.http.post('/api/auth/register', cliente).subscribe({
      next: () => {
        this.cargando = false;
        this.registrado = true;
        this.error = '';

        this.contrasena = '';
        this.confirmarContrasena = '';

        // Actualizar la pantalla
        this.cdr.markForCheck();
      },

      error: (err: HttpErrorResponse) => {
        this.cargando = false;

        if (
          typeof err.error?.message === 'string' &&
          err.error.message.includes('Ya existe el cliente')
        ) {
          this.error = 'Este DNI ya está registrado.';
        } else if (
          err.status === 0 ||
          err.status === 502 ||
          err.status === 503 ||
          err.status === 504
        ) {
          this.error = 'No se pudo conectar con el servidor.';
        } else {
          this.error = 'No se pudo crear la cuenta. Inténtelo nuevamente.';
        }

        // Mostrar el error en pantalla
        this.cdr.markForCheck();
      },
    });
  }
}
