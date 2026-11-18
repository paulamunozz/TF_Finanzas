import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { PageHeader } from '../../shared/ui/page-header/page-header';

import { CatalogProduct } from '../../core/models/catalog-product.model';
import { CatalogStore } from '../../core/data-access/catalog.store';
@Component({
  selector: 'app-products',
  imports: [CurrencyPipe, FormsModule, MatButtonModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './products.html',
  styleUrl: './products.scss',
})
export class Products {
  protected readonly pendingDelete = signal<number | null>(null);
  private editingId: number | null = null;
  protected requestRemove(id: number) {
    this.pendingDelete.set(id);
  }
  protected edit(product: CatalogProduct) {
    this.editingId = product.id;
    this.draft = {
      name: product.name,
      unit: product.unit,
      cashPrice: product.cashPrice,
      creditPrice: product.creditPrice,
    };
    this.showForm.set(true);
  }
  protected readonly query = signal('');
  protected readonly showForm = signal(false);
  private readonly catalog = inject(CatalogStore);
  protected readonly products = this.catalog.products;
  protected readonly error = this.catalog.error;
  protected readonly loading = this.catalog.loading;
  protected draft = { name: '', unit: 'unidad', cashPrice: 0, creditPrice: 0 };
  protected readonly filtered = computed(() =>
    this.products().filter((p) => p.name.toLowerCase().includes(this.query().toLowerCase())),
  );
  protected add() {
    if (!this.draft.name || this.draft.cashPrice <= 0 || this.draft.creditPrice <= 0) return;
    this.catalog.save(this.draft, this.editingId).subscribe({
      next: () => {
        this.editingId = null;
        this.draft = { name: '', unit: 'unidad', cashPrice: 0, creditPrice: 0 };
        this.showForm.set(false);
      },
      error: () => {},
    });
  }
  protected remove(id: number) {
    this.catalog.remove(id).subscribe({ error: () => {} });
  }
}
