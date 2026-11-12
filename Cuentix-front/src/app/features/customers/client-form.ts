import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Client, CreateClientDto } from '../../core/models/client.model';
import { ClientService } from '../../core/services/client.service';
import { PageHeader } from '../../shared/ui/page-header/page-header';

@Component({
  selector: 'app-client-form',
  imports: [ReactiveFormsModule, MatButtonModule, MatSnackBarModule, PageHeader, RouterLink],
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './client-form.html',
  styleUrl: './client-form.scss',
})
export class ClientForm {
  private readonly destroyRef = inject(DestroyRef);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  private readonly service = inject(ClientService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);
  protected readonly id = this.route.snapshot.paramMap.get('id');
  protected readonly form = this.fb.nonNullable.group({
    dni: ['', [Validators.required, Validators.pattern(/^\d{8}$/)]],
    name: ['', Validators.required],
    currency: ['PEN', Validators.required],
    compensatoryRate: [0, [Validators.required, Validators.min(0.000001)]],
    rateType: this.fb.nonNullable.control<Client['rateType']>('TN', Validators.required),
    ratePeriod: this.fb.nonNullable.control<Client['ratePeriod']>('Mensual', Validators.required),
    capitalizationPeriod:
      this.fb.nonNullable.control<NonNullable<Client['capitalizationPeriod']>>('Mensual'),
    lateRate: [0, [Validators.required, Validators.min(0.000001)]],
    lateRateType: this.fb.nonNullable.control<Client['lateRateType']>('TN', Validators.required),
    maxMonths: [1, [Validators.required, Validators.min(1), Validators.max(60)]],
    creditLine: [0, [Validators.required, Validators.min(0.01)]],
    cutOffDay: [1, [Validators.required, Validators.min(1), Validators.max(28)]],
    paymentDay: [1, [Validators.required, Validators.min(1), Validators.max(28)]],
  });
  protected readonly saving = signal(false);
  protected readonly error = signal('');

  constructor() {
    this.form.controls.rateType.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((type) => {
        const control = this.form.controls.capitalizationPeriod;
        if (type === 'TN') control.setValidators(Validators.required);
        else {
          control.clearValidators();
          control.setValue('Mensual', { emitEvent: false });
        }
        control.updateValueAndValidity({ emitEvent: false });
      });
    if (this.id)
      this.service
        .getById(this.id)
        .subscribe({
          next: (client) => this.form.patchValue(client),
          error: () => this.error.set('No se encontró el cliente.'),
        });
  }
  protected save(): void {
    if (this.saving()) return;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set('');
    const raw = this.form.getRawValue();
    const payload: CreateClientDto = {
      ...raw,
      capitalizationPeriod: raw.rateType === 'TN' ? raw.capitalizationPeriod : undefined,
      rateType: raw.rateType,
      lateRateType: raw.lateRateType,
      ratePeriod: raw.ratePeriod,
    };
    const request = this.id ? this.service.update(this.id, payload) : this.service.create(payload);
    request.subscribe({
      next: () => {
        this.snackBar.open(
          this.id ? 'Cliente actualizado correctamente' : 'Cliente registrado correctamente',
          'Cerrar',
          { duration: 3000 },
        );
        this.router.navigate(['/clients']);
      },
      error: () => {
        this.saving.set(false);
        this.error.set('No se pudo guardar el cliente.');
      },
    });
  }
}
