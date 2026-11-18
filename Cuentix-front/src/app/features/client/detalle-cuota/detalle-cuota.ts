import { Component, inject, signal } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import {
  ReporteService,
  DetalleCompra,
  CuotaCliente,
} from '../../../core/services/reporte.service';
@Component({
  selector: 'app-detalle-cuota',
  imports: [DecimalPipe, DatePipe, RouterLink],
  templateUrl: './detalle-cuota.html',
  styleUrl: './detalle-cuota.css',
})
export class DetalleCuota {
  compra = signal<DetalleCompra | null>(null);
  detalle = signal<CuotaCliente | null>(null);
  error = signal('');
  cargando = signal(true);
  constructor() {
    const p = inject(ActivatedRoute).snapshot.paramMap;
    inject(ReporteService)
      .detalle(p.get('purchaseId')!)
      .subscribe({
        next: (r) => {
          this.compra.set(r);
          const c = r.cuotas.find((c) => String(c.id) === p.get('installmentId'));
          this.detalle.set(c ?? null);
          if (!c) this.error.set('La cuota no existe.');
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo consultar la cuota.');
          this.cargando.set(false);
        },
      });
  }
}
