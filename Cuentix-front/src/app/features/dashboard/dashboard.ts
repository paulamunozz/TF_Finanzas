import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin, map, of, switchMap } from 'rxjs';
import { ClientService } from '../../core/services/client.service';
import { PaymentService } from '../../core/services/payment.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';

@Component({
  selector: 'app-dashboard',
  imports: [CurrencyPipe, DatePipe, RouterLink, MatButtonModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  private readonly payments = inject(PaymentService);
  protected readonly today = new Date().toLocaleDateString('en-CA');
  protected readonly data = toSignal(
    inject(ClientService)
      .getAll()
      .pipe(
        switchMap((clients) =>
          (clients.length
            ? forkJoin(
                clients.map((client) =>
                  this.payments
                    .getByClientId(client.id)
                    .pipe(
                      map((items) =>
                        items.map((item) => ({
                          ...item,
                          clientName: client.name,
                          currency: client.currency,
                        })),
                      ),
                    ),
                ),
              )
            : of([])
          ).pipe(map((groups) => ({ clients, items: groups.flat() }))),
        ),
      ),
  );
  protected readonly unpaid = computed(
    () => this.data()?.items.filter((item) => !item.cancelled) ?? [],
  );
  protected readonly overdue = computed(() =>
    this.unpaid().filter((item) => item.dueDate < this.today),
  );
  protected readonly paid = computed(
    () => this.data()?.items.filter((item) => item.cancelled) ?? [],
  );
  protected readonly pending = computed(() =>
    this.unpaid()
      .filter((item) => item.currency === 'PEN')
      .reduce((total, item) => total + item.installmentAmount + item.lateAmount, 0),
  );
  protected readonly upcoming = computed(() =>
    this.unpaid()
      .filter((item) => item.currency === 'PEN' && item.dueDate >= this.today)
      .sort((a, b) => a.dueDate.localeCompare(b.dueDate))
      .slice(0, 3),
  );
  protected readonly statuses = computed(() => [
    {
      label: 'Pendientes',
      count: this.unpaid().length - this.overdue().length,
      color: 'var(--ds-primary)',
    },
    { label: 'Vencidas', count: this.overdue().length, color: 'var(--ds-warning)' },
    { label: 'Canceladas', count: this.paid().length, color: 'var(--ds-success)' },
  ]);
}
