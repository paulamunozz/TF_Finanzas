import { Routes } from '@angular/router';
export const FEATURE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./client-list').then((m) => m.ClientList) },
  { path: 'new', loadComponent: () => import('./client-form').then((m) => m.ClientForm) },
  { path: ':id/edit', loadComponent: () => import('./client-form').then((m) => m.ClientForm) },
  {
    path: ':id/payments',
    loadComponent: () => import('../payment-schedules/payment-report').then((m) => m.PaymentReport),
  },
];
