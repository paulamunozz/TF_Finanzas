import { Routes } from '@angular/router';
import { AuthLayout } from '../../layouts/auth-layout/auth-layout';

export const PUBLIC_ROUTES: Routes = [
  {
    path: '',
    component: AuthLayout,
    children: [
      {
        path: 'login',
        loadComponent: () => import('../client/login/login').then((m) => m.Login),
      },
      {
        path: 'register',
        loadComponent: () => import('../client/registro/registro').then((m) => m.Registro),
      },
      {
        path: '',
        redirectTo: 'login',
        pathMatch: 'full',
      },
    ],
  },
];
