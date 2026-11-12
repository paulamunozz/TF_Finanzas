import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { PageHeader } from '../../shared/ui/page-header/page-header';

@Component({
  selector: 'app-purchases',
  imports: [RouterLink, CurrencyPipe, FormsModule, MatButtonModule, MatSnackBarModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './purchases.html',
  styleUrl: './purchases.scss',
})
export class Purchases {
  private readonly snack = inject(MatSnackBar);
  protected readonly clientId = signal('c1');
  protected mode = 'installments';
  protected months = 3;
  protected readonly today = new Date().toLocaleDateString('en-CA');
  protected purchaseDate = this.today;
  protected readonly items = signal([
    { id: 1, name: 'Café instantáneo', unit: 'kg', price: 20, quantity: 1 },
    { id: 2, name: 'Atún Primor 500 ml', unit: 'unidad', price: 8, quantity: 0 },
    { id: 3, name: 'Leche Laive 300 ml', unit: 'unidad', price: 4.5, quantity: 0 },
  ]);
  protected readonly creditLine = computed(() => (this.clientId() === 'c1' ? 500 : 800));
  protected readonly usedCredit = computed(() => (this.clientId() === 'c1' ? 64.39 : 25));
  protected readonly available = computed(() => this.creditLine() - this.usedCredit());
  protected readonly total = computed(() =>
    this.items().reduce((sum, item) => sum + item.price * item.quantity, 0),
  );
  protected quantity(id: number, value: number) {
    this.items.update((items) =>
      items.map((item) =>
        item.id === id ? { ...item, quantity: Math.max(0, Number(value) || 0) } : item,
      ),
    );
  }
  protected confirm() {
    this.snack.open('Compra registrada en la sesión de demostración', 'Cerrar', { duration: 3500 });
    this.items.update((items) => items.map((item) => ({ ...item, quantity: 0 })));
  }
}
