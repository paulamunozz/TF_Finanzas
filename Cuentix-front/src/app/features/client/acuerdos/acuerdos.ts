import { Component, OnInit, inject, ChangeDetectorRef } from '@angular/core';

import { RouterLink } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';

import { AcuerdosService, AcuerdoClienteDTO } from '../../../core/services/acuerdos.service';

interface Acuerdo {
  id: number;
  tienda: string;
  tasa: string;
  linea: string;
  meses: number;
  fechas: string;
}

@Component({
  selector: 'app-acuerdos',
  imports: [RouterLink],
  templateUrl: './acuerdos.html',
  styleUrl: './acuerdos.css',
})
export class Acuerdos implements OnInit {
  private acuerdosService = inject(AcuerdosService);
  private cdr = inject(ChangeDetectorRef);

  acuerdos: Acuerdo[] = [];

  cargando = false;
  error = '';

  ngOnInit(): void {
    this.cargarAcuerdos();
  }

  cargarAcuerdos(): void {
    this.cargando = true;
    this.error = '';

    this.acuerdosService.obtenerMisAcuerdos().subscribe({
      next: (respuesta: AcuerdoClienteDTO[]) => {
        this.acuerdos = respuesta.map((acuerdo) => ({
          id: acuerdo.id,

          tienda: acuerdo.tienda,

          tasa: acuerdo.tasa !== null ? `${acuerdo.tasa}% ${acuerdo.tipoTasa}` : 'No disponible',

          linea: `${acuerdo.moneda} ${acuerdo.linea.toFixed(2)}`,

          meses: acuerdo.meses,

          fechas: `Corte ${acuerdo.diaCorte} / Pago ${acuerdo.diaPago}`,
        }));

        this.cargando = false;

        // Actualizar la pantalla después de recibir los datos
        this.cdr.markForCheck();
      },

      error: (err: HttpErrorResponse) => {
        this.cargando = false;

        if (err.status === 401) {
          this.error = 'La sesión expiró. Inicie sesión nuevamente.';
        } else if (err.status === 403) {
          this.error = 'No tiene autorización para consultar acuerdos.';
        } else if (err.status === 404) {
          this.error = 'El servicio de acuerdos no está disponible.';
        } else {
          this.error = 'No se pudieron cargar los acuerdos.';
        }

        this.cdr.markForCheck();
      },
    });
  }
}
