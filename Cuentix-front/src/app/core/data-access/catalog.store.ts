import { Injectable, signal } from '@angular/core';
import { CatalogProduct } from '../models/catalog-product.model';

@Injectable({ providedIn: 'root' })
export class CatalogStore {
  private readonly state = signal<CatalogProduct[]>([
    { id: 101, name: 'Café instantáneo', unit: 'kg', cashPrice: 18.9, creditPrice: 20 },
    { id: 102, name: 'Atún Primor 500 ml', unit: 'unidad', cashPrice: 7.5, creditPrice: 8 },
    { id: 103, name: 'Leche Laive 300 ml', unit: 'unidad', cashPrice: 4.2, creditPrice: 4.5 },
  ]);
  readonly products = this.state.asReadonly();
  save(draft: Omit<CatalogProduct, 'id'>, id: number | null): void {
    this.state.update((items) =>
      id === null
        ? [...items, { ...draft, id: Date.now() }]
        : items.map((item) => (item.id === id ? { ...draft, id } : item)),
    );
  }
  remove(id: number): void {
    this.state.update((items) => items.filter((item) => item.id !== id));
  }
}
