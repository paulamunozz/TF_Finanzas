import { FieldHelp } from '../../shared/ui/field-help/field-help';
import { HttpClient } from '@angular/common/http';
import { AsyncPipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatMenuModule } from '@angular/material/menu';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RouterLink } from '@angular/router';
import { catchError, of, startWith, Subject, switchMap } from 'rxjs';
import { Client } from '../../core/models/client.model';
import { ClientService } from '../../core/services/client.service';
import { ReferenceCatalogService } from '../../core/services/reference-catalog.service';
import { DeleteClientDialog } from './delete-client-dialog';
import { PageHeader } from '../../shared/ui/page-header/page-header';

@Component({
  selector: 'app-client-list',
  imports: [
    FieldHelp,
    AsyncPipe,
    CurrencyPipe,
    FormsModule,
    MatMenuModule,
    MatButtonModule,
    MatDialogModule,
    MatSnackBarModule,
    MatTooltipModule,
    PageHeader,
    RouterLink,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './client-list.html',
  styleUrl: './client-list.scss',
})
export class ClientList {
  protected query = '';
  protected currencyId: number | null = null;
  protected readonly currencies = inject(ReferenceCatalogService).currencies();
  protected order = 'name';
  protected readonly page = signal(0);
  protected filtered(clients: Client[]): Client[] {
    const query = this.query.trim().toLocaleLowerCase();
    return clients
      .filter(
        (client) =>
          (this.currencyId === null || client.currencyId === this.currencyId) &&
          (client.name.toLocaleLowerCase().includes(query) || client.dni.includes(query)),
      )
      .sort((a, b) =>
        this.order === 'credit' ? b.creditLine - a.creditLine : a.name.localeCompare(b.name),
      );
  }
  protected initials(name: string): string {
    return name
      .split(' ')
      .map((part) => part[0])
      .slice(0, 2)
      .join('');
  }
  private readonly http = inject(HttpClient);
  protected showInactive = false;
  protected activate(id: string) {
    this.http.put('/api/clients/'+encodeURIComponent(id)+'/activate',{}).subscribe({
      next: () => this.retry(), error: () => this.snackBar.open('No se pudo reactivar la cuenta.', 'Cerrar')});
  }
  private readonly service = inject(ClientService);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly refresh$ = new Subject<void>();
  protected readonly state$ = this.refresh$.pipe(
    startWith(undefined),
    switchMap(() => (this.showInactive ? this.http.get<Client[]>('/api/clients?includeInactive=true') : this.service.getAll()).pipe(catchError(() => { this.error.set(true); return of(null); }))),
  );

  protected readonly error = signal(false);
  protected retry() { this.error.set(false); this.refresh$.next(); }
  protected remove(client: Client): void {
    this.dialog
      .open(DeleteClientDialog, { data: client })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed) {
          this.service.delete(client.id).subscribe({
            next: () => {
              this.page.set(0);
              this.refresh$.next();
              this.snackBar.open('Cliente eliminado correctamente', 'Cerrar', { duration: 3000 });
            },
            error: () =>
              this.snackBar.open('No se pudo eliminar el cliente', 'Cerrar', { duration: 4000 }),
          });
        }
      });
  }
}
