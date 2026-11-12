import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { CLIENT_REPOSITORY } from './core/repositories/client.repository';
import { PAYMENT_REPOSITORY } from './core/repositories/payment.repository';
import { MockClientRepository } from './core/data-access/mock-client.repository';
import { MockPaymentRepository } from './core/data-access/mock-payment.repository';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(),
    { provide: CLIENT_REPOSITORY, useClass: MockClientRepository },
    { provide: PAYMENT_REPOSITORY, useClass: MockPaymentRepository },
  ],
};
