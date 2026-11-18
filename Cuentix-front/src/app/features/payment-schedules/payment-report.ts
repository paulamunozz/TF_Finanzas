import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, map, of, switchMap } from 'rxjs';
import { ScheduleQuery } from '../../core/services/schedule-query.service';
import { Installment } from '../../core/models/payment.model';
import {
  dateKey,
  installmentStatus,
  scheduleSummary,
  statusLabels,
} from '../../financial-engine/domain/schedule-domain';
import { PageHeader } from '../../shared/ui/page-header/page-header';
import { PaymentConfirmDialog } from './payment-confirm-dialog';
@Component({
  selector: 'app-payment-report',
  imports: [CurrencyPipe, DatePipe, FormsModule, RouterLink, MatButtonModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './payment-report.html',
  styleUrl: './payment-report.scss',
})
export class PaymentReport {
  private readonly query = inject(ScheduleQuery);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  protected readonly refresh = signal(0);
  private readonly params = toSignal(this.route.paramMap);
  protected get purchaseId() {
    return this.params()?.get('purchaseId') ?? null;
  }
  protected readonly state = toSignal(
    toObservable(computed(() => ({ refresh: this.refresh(), params: this.params() }))).pipe(
      switchMap(({ params }) => {
        const purchaseId = params?.get('purchaseId');
        const request = purchaseId
          ? this.query.purchase(purchaseId)
          : this.query.customer(params?.get('id') ?? '');
        return request.pipe(
          map((data) => ({ data, error: '' })),
          catchError((error) =>
            of({
              data: null,
              error: error instanceof Error ? error.message : 'No se pudo cargar el cronograma',
            }),
          ),
        );
      }),
    ),
  );
  protected readonly purchaseFilter = signal('');
  protected readonly statusFilter = signal('');
  protected readonly from = signal('');
  protected readonly to = signal('');
  protected readonly order = signal('date');
  protected readonly summary = computed(() => scheduleSummary(this.state()?.data?.items ?? []));
  protected readonly rows = computed(() => {
    const items = this.state()?.data?.items ?? [];
    return items
      .filter(
        (item) =>
          (!this.purchaseFilter() || item.purchaseId === this.purchaseFilter()) &&
          (!this.statusFilter() || installmentStatus(item) === this.statusFilter()) &&
          (!this.from() || dateKey(item.dueDate) >= dateKey(this.from())) &&
          (!this.to() || dateKey(item.dueDate) <= dateKey(this.to())),
      )
      .sort((a, b) =>
        this.order() === 'amount'
          ? b.installmentAmount + b.lateAmount - (a.installmentAmount + a.lateAmount)
          : this.order() === 'status'
            ? installmentStatus(a).localeCompare(installmentStatus(b))
            : dateKey(a.dueDate) - dateKey(b.dueDate),
      );
  });
  protected readonly status = installmentStatus;
  protected readonly labels = statusLabels;
  protected pay(item: Installment) {
    const state = this.state()?.data;
    if (!state || item.cancelled) return;
    this.dialog
      .open(PaymentConfirmDialog, {
        width: '460px',
        maxWidth: '95vw',
        data: {
          item,
          clientName: state.client.name,
          currency:
            state.purchases.find((p) => p.id === item.purchaseId)?.currency ??
            state.client.currency,
        },
      })
      .afterClosed()
      .subscribe((date?: string) => {
        if (date)
          this.query.repository.cancel(item.id, date).subscribe({
            next: () => {
              this.refresh.update((v) => v + 1);
              this.snack.open('Pago completo registrado', 'Cerrar', { duration: 3000 });
            },
            error: (error) => this.snack.open(error.message, 'Cerrar', { duration: 4000 }),
          });
      });
  }
}
