import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { isoToday } from '../../financial-engine/domain/schedule-domain';
import { PaymentConfirmDialog } from './payment-confirm-dialog';

describe('PaymentConfirmDialog', () => {
  const endpoint = '/api/clients/10/payments/2026-09-25';
  const quote = {
    dueDate: '2026-09-25',
    paidAt: isoToday(),
    currency: 'PEN',
    lines: [
      { installmentId: 1, purchaseId: 12, number: 1, late: 2, interest: 0, capital: 200, graceInterest: 0 },
      { installmentId: 2, purchaseId: 13, number: 1, late: 3, interest: 0, capital: 300, graceInterest: 0 },
    ],
    late: 5,
    interest: 0,
    capital: 500,
    total: 505,
  };

  it('confirms the quoted obligations and reuses the key after a failed request', async () => {
    const ref = { close: vi.fn(), disableClose: false };
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MAT_DIALOG_DATA, useValue: {
          item: { id: '1', clientId: '10', dueDate: quote.dueDate, purchaseDate: '2026-09-15' },
          clientName: 'Cliente de prueba',
        } },
        { provide: MatDialogRef, useValue: ref },
      ],
    });
    const component = TestBed.createComponent(PaymentConfirmDialog).componentInstance;
    const http = TestBed.inject(HttpTestingController);
    http.expectOne(`${endpoint}/quote?paidAt=${quote.paidAt}`).flush(quote);

    component['confirm']();
    const first = http.expectOne(`${endpoint}/confirm`);
    expect(first.request.body).toMatchObject({ paidAt: quote.paidAt, amount: 505, installmentIds: [1, 2] });
    expect(first.request.body.requestKey).toMatch(/^[0-9a-f-]{36}$/);
    expect(ref.disableClose).toBe(true);
    first.error(new ProgressEvent('network-error'));
    expect(ref.disableClose).toBe(false);

    component['confirm']();
    const retry = http.expectOne(`${endpoint}/confirm`);
    expect(retry.request.body).toEqual(first.request.body);
    const receipt = { id: 31, payment: quote };
    retry.flush(receipt);
    expect(ref.close).toHaveBeenCalledWith(receipt);
    http.verify();
  });
});
