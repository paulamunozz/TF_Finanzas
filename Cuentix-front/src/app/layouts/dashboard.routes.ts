import { Routes } from '@angular/router';
import { sessionGuard } from '../core/services/session.guard';
import { MainLayout } from './dashboard-layout/main-layout';

export const PRIVATE_ROUTES: Routes = [
  {
    path: '',
    component: MainLayout,
    canActivate: [sessionGuard],
    data: { roles: [1, 3] },
    children: [
      {
        path: 'purchases',
        loadChildren: () =>
          import('../features/purchases/purchases.routes').then((m) => m.FEATURE_ROUTES),
      },
      {
        path: 'clients',
        loadChildren: () =>
          import('../features/customers/customers.routes').then((m) => m.FEATURE_ROUTES),
      },
      {
        path: 'dashboard',
        loadComponent: () => import('../features/dashboard/dashboard').then((m) => m.Dashboard),
      },
      {
        path: 'products',
        loadComponent: () => import('../features/products/products').then((m) => m.Products),
      },

      {
        path: 'clientes/:id/cronograma',
        loadComponent: () =>
          import('../features/payment-schedules/payment-report').then((m) => m.PaymentReport),
      },
      {
        path: 'compras/:purchaseId/cronograma',
        loadComponent: () =>
          import('../features/payment-schedules/payment-report').then((m) => m.PaymentReport),
      },
      {
        path: 'cuotas/:paymentId',
        loadComponent: () =>
          import('../features/payment-schedules/installment-detail').then(
            (m) => m.InstallmentDetail,
          ),
      },

      {
        path: 'installments/:paymentId',
        loadComponent: () =>
          import('../features/payment-schedules/installment-detail').then(
            (m) => m.InstallmentDetail,
          ),
      },
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
    ],
  },
];
