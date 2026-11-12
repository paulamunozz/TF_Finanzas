import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { ScheduleQuery } from '../../core/services/schedule-query.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';
@Component({
  selector: 'app-purchase-history',
  imports: [CurrencyPipe, DatePipe, MatButtonModule, RouterLink, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './purchase-history.html',
  styleUrl: './purchase-history.scss',
})
export class PurchaseHistory {
  protected readonly purchases = toSignal(inject(ScheduleQuery).repository.getPurchases());
}
