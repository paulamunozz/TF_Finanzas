import { Pipe, PipeTransform } from '@angular/core';
import { Installment } from '../../core/models/payment.model';

@Pipe({ name: 'totalPayments', standalone: true })
export class TotalPaymentsPipe implements PipeTransform {
  transform(items: Installment[]): number {
    return items.reduce((total, item) => total + item.installmentAmount + item.lateAmount, 0);
  }
}
