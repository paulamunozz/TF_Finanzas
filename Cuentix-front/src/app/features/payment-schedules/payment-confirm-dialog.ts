import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { Subscription } from 'rxjs';
import { API_BASE_URL } from '../../core/config/api.config';
import { Installment } from '../../core/models/payment.model';
import { dateKey, isoToday } from '../../financial-engine/domain/schedule-domain';
import { FieldHelp } from '../../shared/ui/field-help/field-help';

interface PaymentLine {
  installmentId: number;
  purchaseId: number;
  number: number;
  late: number;
  interest: number;
  capital: number;
  graceInterest: number;
}

interface PaymentQuote {
  dueDate: string;
  paidAt: string;
  currency: string;
  lines: PaymentLine[];
  late: number;
  interest: number;
  capital: number;
  total: number;
}

export interface PaymentReceipt {
  id: number;
  payment: PaymentQuote;
}

export interface PaymentDialogData {
  item: Installment;
  clientName: string;
}

@Component({
  selector: 'app-payment-confirm',
  imports: [FieldHelp, CurrencyPipe, DatePipe, FormsModule, MatDialogModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './payment-confirm-dialog.html',
  styleUrl: './payment-confirm-dialog.scss',
})
export class PaymentConfirmDialog {
  protected readonly data = inject<PaymentDialogData>(MAT_DIALOG_DATA);
  private readonly ref = inject<MatDialogRef<PaymentConfirmDialog, PaymentReceipt>>(MatDialogRef);
  private readonly http = inject(HttpClient);
  private readonly destroyRef = inject(DestroyRef);
  private readonly api = inject(API_BASE_URL);
  private readonly endpoint =
    `${this.api}/clients/${encodeURIComponent(this.data.item.clientId)}/payments/${this.data.item.dueDate}`;
  private pending?: Subscription;
  private requestKey?: string;
  protected readonly today = isoToday();
  protected date = this.today;
  protected readonly quote = signal<PaymentQuote | null>(null);
  protected readonly quoteError = signal('');
  protected readonly confirmationError = signal('');
  protected readonly saving = signal(false);

  constructor() {
    this.load(this.today);
  }

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

  protected load(date: string) {
    if (this.saving()) return;
    this.date = date;
    this.pending?.unsubscribe();
    this.quote.set(null);
    this.quoteError.set('');
    this.confirmationError.set('');
    this.requestKey = undefined;
    if (!this.valid()) return;
    this.pending = this.http
      .get<PaymentQuote>(`${this.endpoint}/quote`, { params: { paidAt: date } })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (quote) => this.quote.set(quote),
        error: (error: HttpErrorResponse) =>
          this.quoteError.set(error.error?.message ?? 'No se pudo consultar el importe para esta fecha.'),
      });
  }

  protected confirm() {
    const quote = this.quote();
    if (!quote || this.saving()) return;
    this.requestKey ??= globalThis.crypto.randomUUID();
    this.saving.set(true);
    this.confirmationError.set('');
    this.ref.disableClose = true;
    this.http
      .post<PaymentReceipt>(`${this.endpoint}/confirm`, {
        paidAt: quote.paidAt,
        amount: quote.total,
        installmentIds: quote.lines.map((line) => line.installmentId),
        requestKey: this.requestKey,
      })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (receipt) => this.ref.close(receipt),
        error: (error: HttpErrorResponse) => {
          this.saving.set(false);
          this.ref.disableClose = false;
          if (error.status === 409) this.load(this.date);
          this.confirmationError.set(error.error?.message ?? 'No se pudo registrar el pago. Reintenta.');
        },
      });
  }
}
