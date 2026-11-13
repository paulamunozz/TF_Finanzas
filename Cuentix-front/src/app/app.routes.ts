import { Routes } from '@angular/router';
export const routes: Routes = [
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
    loadChildren: () => import('./layouts/dashboard.routes').then((m) => m.PRIVATE_ROUTES),
  },
  { path: '**', redirectTo: 'auth/login' },
];
