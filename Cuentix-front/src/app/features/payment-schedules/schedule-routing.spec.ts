import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { routes } from '../../app.routes';
import { CLIENT_REPOSITORY } from '../../core/repositories/client.repository';
import { PAYMENT_REPOSITORY } from '../../core/repositories/payment.repository';
import { MockClientRepository } from '../../core/data-access/mock-client.repository';
import { MockPaymentRepository } from '../../core/data-access/mock-payment.repository';

describe('Schedule navigation', () => {
  beforeEach(() =>
    TestBed.configureTestingModule({
      providers: [
        provideRouter(routes),
        { provide: CLIENT_REPOSITORY, useClass: MockClientRepository },
        { provide: PAYMENT_REPOSITORY, useClass: MockPaymentRepository },
      ],
    }),
  );
  it('navigates from consolidated customer to purchase and installment, preserving legacy routes', async () => {
    const harness = await RouterTestingHarness.create();
    await harness.navigateByUrl('/clients/c1/payments');
    await harness.fixture.whenStable();
    harness.detectChanges();
    await expect
      .poll(() => {
        harness.detectChanges();
        return harness.routeNativeElement?.textContent;
      })
      .toContain('Cronograma de pagos');
    await harness.navigateByUrl('/purchases/COMP-002/schedule');
    await harness.fixture.whenStable();
    harness.detectChanges();
    await expect
      .poll(() => {
        harness.detectChanges();
        return harness.routeNativeElement?.textContent;
      })
      .toContain('Cronograma · COMP-002');
    await harness.navigateByUrl('/installments/COMP-002-1');
    await harness.fixture.whenStable();
    harness.detectChanges();
    await expect
      .poll(() => {
        harness.detectChanges();
        return harness.routeNativeElement?.textContent;
      })
      .toContain('Cuota 1 de 4');
    await harness.navigateByUrl('/cuotas/COMP-001-1');
    await harness.fixture.whenStable();
    harness.detectChanges();
    await expect
      .poll(() => {
        harness.detectChanges();
        return harness.routeNativeElement?.textContent;
      })
      .toContain('Cuota 1 de 1');
  });
});
