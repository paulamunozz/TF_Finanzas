import { HttpClient } from '@angular/common/http';
import { Subscription } from 'rxjs';
import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal, OnDestroy } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Installment } from '../../core/models/payment.model';
import {
  dateKey,
  isoToday,

  sumMoney,
} from '../../financial-engine/domain/schedule-domain';
@Component({
  selector: 'app-payment-confirm',
  imports: [CurrencyPipe, FormsModule, MatDialogModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './payment-confirm-dialog.html',
  styleUrl: './payment-confirm-dialog.scss',
})
export class PaymentConfirmDialog implements OnDestroy {
  protected readonly data = inject<{ item: Installment; clientName: string; currency: string }>(
    MAT_DIALOG_DATA,
  );
  protected readonly ref = inject(MatDialogRef<PaymentConfirmDialog>);
  protected readonly today = isoToday();
  protected date = this.today;
  protected valid() {
    try {
      return (
        dateKey(this.date) <= dateKey(this.today) &&
        dateKey(this.date) >= dateKey(this.data.item.purchaseDate)
      );
    } catch {
      return false;
    }
  }
  private readonly http = inject(HttpClient);
  private pending?: Subscription;
  protected readonly quote = signal<Installment | null>(null);
  protected readonly error = signal('');
  constructor() { this.load(this.today); }
  ngOnDestroy() { this.pending?.unsubscribe(); }
  protected load(date: string) {
    this.date = date;
    this.pending?.unsubscribe();
    this.quote.set(null);
    this.error.set('');
    if (!this.valid()) return;
    this.pending = this.http.get<Installment>('/api/payments/' + encodeURIComponent(this.data.item.id) + '/quote',
      {params: {paidAt: date}}).subscribe({
      next: q => this.quote.set(q),
      error: () => this.error.set('No se pudo consultar el importe para esta fecha.'),
    });
  }
  protected total() { return sumMoney([this.data.item.installmentAmount, this.quote()?.lateAmount ?? 0]); }
}
