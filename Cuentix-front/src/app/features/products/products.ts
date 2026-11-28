import { FieldHelp } from '../../shared/ui/field-help/field-help';
import { MatMenuModule } from '@angular/material/menu';
import { HttpClient } from '@angular/common/http';
import { DecimalPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { PageHeader } from '../../shared/ui/page-header/page-header';

import { CatalogProduct } from '../../core/models/catalog-product.model';
import { CatalogStore } from '../../core/data-access/catalog.store';
import { ReferenceCatalogService } from '../../core/services/reference-catalog.service';
@Component({
  selector: 'app-products',
  imports: [FieldHelp, MatMenuModule, DecimalPipe, FormsModule, MatButtonModule, PageHeader],
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
    const { id, unit, unitId, ...fields } = product;
    this.editingId = id;
    this.draft = { ...this.blank(), ...fields, unitId };
    this.showForm.set(true);
  }
  protected readonly query = signal('');
  protected readonly showForm = signal(false);
  protected readonly units = inject(ReferenceCatalogService).units(this.showForm);
  private readonly catalog = inject(CatalogStore);
  private readonly http = inject(HttpClient);
  protected readonly inactive = signal<CatalogProduct[]>([]);
  protected showInactive = false;
  protected loadInactive() {
    if (!this.showInactive) return;
    this.http.get<CatalogProduct[]>('/api/catalog?includeInactive=true').subscribe({
      next: rows => this.inactive.set(rows.filter(p => p.active === false)),
      error: () => this.error.set('No se pudieron cargar los productos inactivos.')});
  }
  protected activate(id: number) {
    this.http.put('/api/catalog/'+id+'/activate',{}).subscribe({
      next: () => {this.catalog.load(); this.loadInactive();},
      error: () => this.error.set('No se pudo reactivar el producto.')});
  }
  protected readonly products = this.catalog.products;
  protected readonly includeInactive = signal(false);
  protected readonly error = this.catalog.error;
  protected readonly loading = this.catalog.loading;
  private blank(): Omit<CatalogProduct, 'id' | 'unit'> {
    return {
      name: '', unitId: null, cashPrice: 0, creditPrice: 0,
      description: '', brand: '', supplier: '', imageUrl: '', installmentsAllowed: false,
    };
  }
  protected draft = this.blank();
  protected readonly saving = signal(false);
  protected newProduct() {
    this.editingId = null;
    this.draft = this.blank();
    this.showForm.set(true);
  }
  protected readonly filtered = computed(() =>
    [...this.products(), ...(this.includeInactive() ? this.inactive() : [])].filter((p) => p.name.toLowerCase().includes(this.query().toLowerCase())),
  );
  protected add() {
    const { unitId, ...fields } = this.draft;
    if (!fields.name || fields.cashPrice <= 0 || fields.creditPrice <= 0) return;
    if (this.saving() || this.units.isLoading() || !this.units.hasValue() || unitId == null) return;
    this.saving.set(true);
    this.catalog.save({ ...fields, unitId }, this.editingId).subscribe({
      next: () => {
        this.saving.set(false);
        this.editingId = null;
        this.draft = this.blank();
        this.showForm.set(false);
      },
      error: () => this.saving.set(false),
    });
  }
  protected remove(id: number) {
    this.catalog.remove(id).subscribe({ next: () => this.loadInactive(), error: () => {} });
  }
}
