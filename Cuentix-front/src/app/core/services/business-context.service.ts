import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
@Injectable({ providedIn: 'root' })
export class BusinessContext {
  readonly id = signal('');
  readonly name = signal('');
  readonly error = signal('');
  constructor() {
    inject(HttpClient, { optional: true })
      ?.get<{ id: string; name: string }>('/api/business/context')
      .subscribe({
        next: (c) => {
          this.id.set(c.id);
          this.name.set(c.name);
        },
        error: () => this.error.set('No se pudo cargar el negocio activo.'),
      });
  }
}
