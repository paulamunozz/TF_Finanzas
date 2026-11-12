import { Injectable, signal } from '@angular/core';
@Injectable({ providedIn: 'root' })
export class BusinessContext {
  readonly id = signal('b1');
  readonly name = signal('Bodega Mariela');
}
