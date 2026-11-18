import { Routes } from '@angular/router';
import { sessionGuard } from './core/services/session.guard';
export const routes: Routes = [
  {path: 'admin/negocios', canActivate: [sessionGuard], data: {roles: [1]},
    loadComponent: () => import('./features/admin/business-admin').then(m => m.BusinessAdmin)},
  { path: 'login', redirectTo: 'auth/login', pathMatch: 'full' },
  { path: 'registro', redirectTo: 'auth/register', pathMatch: 'full' },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.PUBLIC_ROUTES),
  },
  {
    path: 'cliente',
    loadChildren: () => import('./features/client/client.routes').then((m) => m.CLIENT_ROUTES),
  },
  {
    path: '',
    loadChildren: () => import('./layouts/dashboard.routes').then((m) => m.DASHBOARD_ROUTES),
  },
  { path: '**', redirectTo: 'auth/login' },
];
