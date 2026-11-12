import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then((m) => m.PUBLIC_ROUTES),
  },
  {
    path: '',
    loadChildren: () => import('./layouts/dashboard.routes').then((m) => m.PRIVATE_ROUTES),
  },
  {
    path: '**',
    redirectTo: '',
  },
];
