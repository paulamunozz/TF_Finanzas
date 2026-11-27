import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { catchError, forkJoin, map, of, switchMap } from 'rxjs';
import { ScheduleQuery } from '../../core/services/schedule-query.service';
import { ClientService } from '../../core/services/client.service';
import { installmentStatus, statusLabels } from '../../financial-engine/domain/schedule-domain';
import { PaymentConfirmDialog, type PaymentDialogData, type PaymentReceipt } from './payment-confirm-dialog';
import { PageHeader } from '../../shared/ui/page-header/page-header';
@Component({
  selector: 'app-installment-detail',
  imports: [CurrencyPipe, DatePipe, RouterLink, MatButtonModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './installment-detail.html',
  styleUrl: './installment-detail.scss',
})
export class InstallmentDetail {
  private readonly repository = inject(ScheduleQuery).repository;
  private readonly clients = inject(ClientService);
  private readonly route = inject(ActivatedRoute);
  private readonly dialog = inject(MatDialog);
  private readonly snack = inject(MatSnackBar);
  protected readonly refresh = signal(0);
  private readonly params = toSignal(this.route.paramMap);
  protected readonly state = toSignal(
    toObservable(
      computed(() => ({ refresh: this.refresh(), id: this.params()?.get('paymentId') })),
    ).pipe(
      switchMap(({ id }) =>
        this.repository.getById(id ?? '').pipe(
          switchMap((item) =>
            forkJoin({
              item: of(item),
              purchase: this.repository.getPurchase(item.purchaseId),
              client: this.clients.getById(item.clientId),
            }),
          ),
          map((data) => ({ data, error: '' })),
          catchError((error) => of({ data: null, error: error.message as string })),
        ),
      ),
    ),
  );
  protected readonly status = installmentStatus;
  protected readonly labels = statusLabels;
  protected pay() {
    const state = this.state()?.data;
    if (!state || state.item.cancelled) return;
    this.dialog
      .open<PaymentConfirmDialog, PaymentDialogData, PaymentReceipt>(PaymentConfirmDialog, {
        width: '520px',
        maxWidth: '95vw',
        data: {
          item: state.item,
          clientName: state.client.name,
        },
      })
      .afterClosed()
      .subscribe((receipt) => {
        if (!receipt) return;
        this.refresh.update((v) => v + 1);
        this.snack.open(`Pago completo registrado · Recibo ${receipt.id}`, 'Cerrar', { duration: 4000 });
      });
  }
}
