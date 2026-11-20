import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { DecimalPipe } from '@angular/common';
import { AcuerdosService, AcuerdoClienteDTO } from '../../../core/services/acuerdos.service';
@Component({
  selector: 'app-acuerdo-detail',
  imports: [RouterLink, DecimalPipe],
  templateUrl: './acuerdo-detail.html',
})
export class AcuerdoDetail {
  acuerdo = signal<AcuerdoClienteDTO | null>(null);
  error = signal('');
  cargando = signal(true);
  constructor() {
    const id = inject(ActivatedRoute).snapshot.paramMap.get('accountId');
    inject(AcuerdosService)
      .obtenerMisAcuerdos()
      .subscribe({
        next: (r) => {
          this.acuerdo.set(r.find((a) => String(a.id) === id) ?? null);
          if (!this.acuerdo()) this.error.set('El acuerdo no existe o no está disponible.');
          this.cargando.set(false);
        },
        error: () => {
          this.error.set('No se pudo consultar el acuerdo.');
          this.cargando.set(false);
        },
      });
  }
}
