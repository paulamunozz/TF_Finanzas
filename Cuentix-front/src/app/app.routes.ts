import { Routes } from '@angular/router';

export const routes: Routes = [
  {
    path: 'auth',
    loadChildren: () =>
      import('./public/public.routes')
        .then(m => m.PUBLIC_ROUTES),
  },
  {
    path: '',
    loadChildren: () =>
      import('./private/private.routes')
        .then(m => m.PRIVATE_ROUTES),
  },
  {
    path: '**',
    redirectTo: '',
  },
];