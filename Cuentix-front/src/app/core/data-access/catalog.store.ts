import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { tap, catchError, throwError } from 'rxjs';
import { CatalogProduct } from '../models/catalog-product.model';
@Injectable({ providedIn: 'root' })
export class CatalogStore {
  private http = inject(HttpClient);
  private state = signal<CatalogProduct[]>([]);
  readonly products = this.state.asReadonly();
  readonly error = signal('');
  readonly loading = signal(true);
  constructor() {
    this.load();
  }
  load() {
    this.loading.set(true);
    this.error.set('');
    this.http.get<CatalogProduct[]>('/api/catalog').subscribe({
      next: (r) => {
        this.state.set(r);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el catálogo.');
        this.loading.set(false);
      },
    });
  }
  save(draft: Omit<CatalogProduct, 'id'>, id: number | null) {
    this.error.set('');
    const request =
      id === null
        ? this.http.post<CatalogProduct>('/api/catalog', draft)
        : this.http.put<CatalogProduct>('/api/catalog/' + id, draft);
    return request.pipe(
      tap(() => this.load()),
      catchError((e) => {
        this.error.set(e.error?.message ?? 'No se pudo guardar el producto.');
        return throwError(() => e);
      }),
    );
  }
  remove(id: number) {
    return this.http.delete<void>('/api/catalog/' + id).pipe(
      tap(() => this.load()),
      catchError((e) => {
        this.error.set('No se pudo eliminar el producto.');
        return throwError(() => e);
      }),
    );
  }
}
