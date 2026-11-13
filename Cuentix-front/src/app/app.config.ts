import { ApplicationConfig, provideBrowserGlobalErrorListeners } from '@angular/core';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { sessionInterceptor } from './core/services/session.interceptor';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { CLIENT_REPOSITORY } from './core/repositories/client.repository';
import { PAYMENT_REPOSITORY } from './core/repositories/payment.repository';
import { HttpClientRepository } from './core/data-access/http-client.repository';
import { HttpPaymentRepository } from './core/data-access/http-payment.repository';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes),
    provideHttpClient(withInterceptors([sessionInterceptor])),
    { provide: CLIENT_REPOSITORY, useClass: HttpClientRepository },
    { provide: PAYMENT_REPOSITORY, useClass: HttpPaymentRepository },
  ],
};
