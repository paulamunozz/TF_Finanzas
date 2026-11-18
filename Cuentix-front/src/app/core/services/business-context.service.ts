import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
@Injectable({ providedIn: 'root' })
export class BusinessContext {
  readonly id = signal('');
  readonly name = signal('');
  readonly error = signal('');
  private http = inject(HttpClient, { optional: true });
  load() {
    this.id.set('');
    this.name.set('');
    this.error.set('');
    this.http
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
