import { FieldHelp } from '../../shared/ui/field-help/field-help';
import { CurrencyPipe, DatePipe, PercentPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Router, RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { ClientService } from '../../core/services/client.service';
import { Client } from '../../core/models/client.model';
import { CatalogStore } from '../../core/data-access/catalog.store';
import { isoToday } from '../../financial-engine/domain/schedule-domain';
@Component({
  selector: 'app-purchases',
  imports: [FieldHelp, DatePipe, PercentPipe, RouterLink, CurrencyPipe, FormsModule, MatButtonModule, MatSnackBarModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './purchases.html',
  styleUrl: './purchases.scss',
})
export class Purchases {
  private http = inject(HttpClient);
  private router = inject(Router);
  private snack = inject(MatSnackBar);
  protected catalog = inject(CatalogStore);
  protected clients = signal<Client[]>([]);
  protected clientId = signal('');
  protected mode = 'installments';
  protected months = 3;
  protected readonly today = isoToday();
  protected purchaseDate = this.today;
  protected quantities = signal<Record<number, number>>({});
  protected error = signal('');
  protected saving = signal(false);
  protected readonly selected = computed(() =>
    this.clients().find((c) => c.id === this.clientId()),
  );
  protected readonly currency = computed(() => this.selected()?.currency ?? 'PEN');
  protected readonly creditLine = computed(() => this.selected()?.creditLine ?? 0);
  protected readonly usedCredit = computed(() => this.selected()?.usedCredit ?? 0);
  protected readonly available = computed(() => this.creditLine() - this.usedCredit());
  protected readonly maxMonths = computed(() => this.selected()?.maxMonths ?? 1);
  protected readonly items = computed(() =>
    this.catalog
      .products()
      .map((p) => ({ ...p, price: p.creditPrice, quantity: this.quantities()[p.id] ?? 0 })),
  );
  protected readonly total = computed(() =>
    this.items().reduce((s, p) => s + p.price * p.quantity, 0),
  );
  constructor() {
    inject(ClientService)
      .getAll()
      .subscribe({
        next: (r) => {
          this.clients.set(r);
          this.clientId.set(r[0]?.id ?? '');
        },
        error: () => this.error.set('No se pudieron cargar las cuentas de crédito.'),
      });
  }
  protected quantity(id: number, value: number) {
    this.quantities.update((q) => ({ ...q, [id]: Math.max(0, Math.floor(Number(value) || 0)) }));
  }
  protected purchaseTime = '';
  protected readonly quote = signal<PurchaseQuote | null>(null);
  private quotedRequest = '';
  private request() {
    return {accountId: Number(this.clientId()), months: this.months, mode: this.mode,
      purchaseDate: this.purchaseDate, purchaseTime: this.purchaseTime || null,
      items: this.items().filter(i => i.quantity > 0).map(i => ({productId: i.id, quantity: i.quantity}))};
  }
  protected currentQuote() {
    return this.quotedRequest === JSON.stringify(this.request()) ? this.quote() : null;
  }
  protected confirm() {
    if (this.saving()) return;
    const request = this.request();
    const current = this.currentQuote();
    this.saving.set(true);
    this.error.set('');
    if (!current) {
      this.http.post<PurchaseQuote>('/api/business/purchases/quote', request).subscribe({
        next: quote => { this.quotedRequest = JSON.stringify(request); this.quote.set(quote); this.saving.set(false); },
        error: e => { this.saving.set(false); this.error.set(e.error?.message ?? 'No se pudo cotizar la compra.'); }
      });
      return;
    }
    this.http.post<{id: string}>('/api/business/purchases', {...request, expected: current})
      .subscribe({
        next: (p) => {
          this.saving.set(false);
          this.snack.open('Compra y cronograma guardados', 'Cerrar', { duration: 3000 });
          this.router.navigate(['/purchases', p.id, 'schedule']);
        },
        error: (e) => {
          this.quote.set(null);
          this.error.set(e.error?.message ?? 'No se pudo registrar la compra.');
          this.saving.set(false);
        },
      });
  }
}

interface PurchaseQuote {
  cutoff: string;
  firstDue: string;
  paymentDay: string;
  principal: number;
  graceInterest: number;
  total: number;
  annualRate: number;
  availableBefore: number;
  availableAfter: number;
}
