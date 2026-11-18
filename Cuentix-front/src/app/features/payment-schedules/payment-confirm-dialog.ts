import { CurrencyPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { Installment } from '../../core/models/payment.model';
import {
  dateKey,
  isoToday,
  quoteMora,
  sumMoney,
} from '../../financial-engine/domain/schedule-domain';
@Component({
  selector: 'app-payment-confirm',
  imports: [CurrencyPipe, FormsModule, MatDialogModule, MatButtonModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './payment-confirm-dialog.html',
  styleUrl: './payment-confirm-dialog.scss',
})
export class PaymentConfirmDialog {
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
  protected quote() {
    return quoteMora(this.data.item, this.valid() ? this.date : this.today);
  }
  protected total() {
    return sumMoney([this.data.item.installmentAmount, this.quote().lateAmount]);
  }
}
