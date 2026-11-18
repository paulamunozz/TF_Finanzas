import { Component, inject, signal } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { ReporteService, DetalleCompra } from '../../../core/services/reporte.service';
@Component({
  selector: 'app-detalle-reporte',
  imports: [DecimalPipe, DatePipe, RouterLink],
  templateUrl: './detalle-reporte.html',
  styleUrl: './detalle-reporte.css',
})
export class DetalleReporte {
  detalle = signal<DetalleCompra | null>(null);
  error = signal('');
  cargando = signal(true);
  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('purchaseId')!;
    inject(ReporteService)
      .detalle(id)
      .subscribe({
        next: (r) => {
          this.detalle.set(r);
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo consultar la compra.');
          this.cargando.set(false);
        },
      });
  }
}
