import { Component, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { ReporteService, CompraCliente } from '../../../core/services/reporte.service';
@Component({
  selector: 'app-reporte',
  imports: [DecimalPipe, RouterLink],
  templateUrl: './reporte.html',
  styleUrl: './reporte.css',
})
export class Reporte {
  filas = signal<CompraCliente[]>([]);
  cargando = signal(true);
  error = signal('');
  constructor() {
    inject(ReporteService)
      .compras()
      .subscribe({
        next: (r) => {
          this.filas.set(r);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudieron cargar sus compras. Intente nuevamente.');
          this.cargando.set(false);
        },
      });
  }
}
