import { Routes } from '@angular/router';
import { sessionGuard } from '../../core/services/session.guard';
export const CLIENT_ROUTES: Routes = [
  {
    path: '',
    canActivate: [sessionGuard],
    data: { roles: [2] },
    loadComponent: () => import('./panel-cliente/panel-cliente').then((m) => m.PanelCliente),
    children: [
      { path: '', redirectTo: 'reporte', pathMatch: 'full' },
      { path: 'reporte', loadComponent: () => import('./reporte/reporte').then((m) => m.Reporte) },
      {
        path: 'reporte/detalle/:purchaseId',
        loadComponent: () =>
          import('./detalle-reporte/detalle-reporte').then((m) => m.DetalleReporte),
      },
      {
        path: 'reporte/detalle/:purchaseId/cuota/:installmentId',
        loadComponent: () => import('./detalle-cuota/detalle-cuota').then((m) => m.DetalleCuota),
      },
      {
        path: 'acuerdos/:accountId',
        loadComponent: () => import('./acuerdos/acuerdo-detail').then((m) => m.AcuerdoDetail),
      },
      {
        path: 'acuerdos',
        loadComponent: () => import('./acuerdos/acuerdos').then((m) => m.Acuerdos),
      },
      { path: 'perfil', loadComponent: () => import('./perfil/perfil').then((m) => m.Perfil) },
      {
        path: 'perfil/editar',
        loadComponent: () => import('./editar-perfil/editar-perfil').then((m) => m.EditarPerfil),
      },
    ],
  },
];
