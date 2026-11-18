import { Routes } from '@angular/router';
export const FEATURE_ROUTES: Routes = [
  { path: '', loadComponent: () => import('./purchases').then((m) => m.Purchases) },
  {
    path: 'history',
    loadComponent: () =>
      import('../payment-schedules/purchase-history').then((m) => m.PurchaseHistory),
  },
  {
    path: ':purchaseId/schedule',
    loadComponent: () => import('../payment-schedules/payment-report').then((m) => m.PaymentReport),
  },
];
