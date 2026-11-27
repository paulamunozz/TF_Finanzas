import { FieldHelp } from '../../shared/ui/field-help/field-help';
import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { HttpClient } from '@angular/common/http';
import { MatButtonModule } from '@angular/material/button';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { API_BASE_URL } from '../../core/config/api.config';
import { isoToday, sumMoney } from '../../financial-engine/domain/schedule-domain';
import { PageHeader } from '../../shared/ui/page-header/page-header';

interface Entry {
  id: number;
  purchaseId: number;
  purchaseDate: string;
  number: number;
  dueDate: string;
  capital: number;
  interest: number;
  graceInterest: number;
  late: number;
  paidAt: string | null;
  paidLate: number | null;
  adjustment: boolean;
}

interface Report {
  id: number;
  customer: string;
  currency: string;
  paymentDay: string;
  entries: Entry[];
}

@Component({
  selector: 'app-cutoff-list',
  imports: [FieldHelp, PageHeader, CurrencyPipe, DatePipe, MatButtonModule, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './cutoff-list.html',
})
export class CutoffList {
  private readonly http = inject(HttpClient);
  private readonly api = inject(API_BASE_URL);
  private readonly destroyRef = inject(DestroyRef);
  private pending?: Subscription;
  protected month = isoToday().slice(0, 7);
  protected readonly reports = signal<Report[] | null>(null);
  protected readonly error = signal('');

  constructor() {
    this.load(this.month);
  }

  protected load(month: string) {
    if (!month) return;
    this.pending?.unsubscribe();
    this.month = month;
    this.reports.set(null);
    this.error.set('');
    this.pending = this.http.get<Report[]>(`${this.api}/business/cutoff`, { params: { month } })
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (reports) => this.reports.set(reports),
        error: () => this.error.set('No se pudieron cargar los listados.'),
      });
  }

  protected total(entries: Entry[]) {
    return sumMoney(entries.flatMap((entry) => [entry.capital, entry.interest, entry.late]));
  }
}
