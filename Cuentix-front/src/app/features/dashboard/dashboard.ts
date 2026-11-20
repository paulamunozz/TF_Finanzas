import { FieldHelp } from '../../shared/ui/field-help/field-help';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { forkJoin } from 'rxjs';
import { Client } from '../../core/models/client.model';
import { Installment } from '../../core/models/payment.model';
import { PageHeader } from '../../shared/ui/page-header/page-header';

@Component({
  selector: 'app-dashboard',
  imports: [FieldHelp, CurrencyPipe, DatePipe, RouterLink, MatButtonModule, PageHeader],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.scss',
})
export class Dashboard {
  protected readonly error = signal('');
  private readonly http = inject(HttpClient);
  protected readonly today = new Intl.DateTimeFormat('en-CA', {timeZone: 'America/Lima'}).format(new Date());
  protected readonly data = signal<{clients: Client[]; items: (Installment & {clientName: string; currency: string})[]} | null>(null);
  protected readonly currency = signal('PEN');
  protected readonly currencies = computed(() => [...new Set(this.data()?.clients.map(c => c.currency))].sort());
  constructor() { this.load(); }
  protected load() {
    this.error.set('');
    this.data.set(null);
    forkJoin({
      clients: this.http.get<Client[]>('/api/clients'),
      payments: this.http.get<Installment[]>('/api/payments'),
    }).subscribe({
      next: ({clients, payments}) => {
        const accounts = new Map(clients.map(c => [c.id, c]));
        const items = payments.flatMap(item => {
          const account = accounts.get(item.clientId);
          return account ? [{...item, clientName: account.name, currency: account.currency}] : [];
        });
        this.data.set({clients, items});
        if (!this.currencies().includes(this.currency())) this.currency.set(this.currencies()[0] ?? 'PEN');
      },
      error: () => this.error.set('No se pudo cargar la cartera.'),
    });
  }
  protected readonly items = computed(() => this.data()?.items.filter(i => i.currency === this.currency()) ?? []);
  protected readonly unpaid = computed(() => this.items().filter(i => !i.cancelled));
  protected readonly overdue = computed(() => this.unpaid().filter(i => i.dueDate < this.today));
  protected readonly paid = computed(() => this.items().filter(i => i.cancelled));
  protected readonly pending = computed(() => this.unpaid().reduce((sum,i) => sum + i.installmentAmount + i.lateAmount, 0));
  protected readonly pastDue = computed(() => this.overdue().reduce((sum,i) => sum + i.installmentAmount + i.lateAmount, 0));
  protected readonly active = computed(() => new Set(this.unpaid().map(i => i.purchaseId)).size);
  protected readonly upcoming = computed(() => this.unpaid().filter(i => i.dueDate >= this.today)
    .sort((a,b) => a.dueDate.localeCompare(b.dueDate)).slice(0,5));
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
