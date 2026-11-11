
import { Routes, CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';

import { Login } from './pages/login/login';
import { Registro } from './pages/registro/registro';
import { PanelCliente } from './pages/panel-cliente/panel-cliente';
import { Reporte } from './pages/reporte/reporte';
import { Acuerdos } from './pages/acuerdos/acuerdos';
import { Perfil } from './pages/perfil/perfil';
import { EditarPerfil } from './pages/editar-perfil/editar-perfil';
import { DetalleReporte } from './pages/detalle-reporte/detalle-reporte';
import { DetalleCuota } from './pages/detalle-cuota/detalle-cuota';



// Protección básica de navegación para el prototipo
const validarSesion: CanActivateFn = () => {
  const router = inject(Router);
  const usuario = sessionStorage.getItem('cuentix_usuario');

  return usuario
    ? true
    : router.createUrlTree(['/login']);
};

export const routes: Routes = [

  {
    path: '',
    redirectTo: 'login',
    pathMatch: 'full'
  },

  {
    path: 'login',
    component: Login
  },

  {
    path: 'registro',
    component: Registro
  },

  {
    path: 'cliente',
    component: PanelCliente,
    canActivate: [validarSesion],
    children: [

      {
        path: '',
        redirectTo: 'reporte',
        pathMatch: 'full'
      },

{
  path: 'reporte/detalle/cuota',
  component: DetalleCuota
},
  {
  path: 'reporte/detalle',
  component: DetalleReporte
},
      {
        path: 'reporte',
        component: Reporte
      },

      {
        path: 'acuerdos',
        component: Acuerdos
      },

      {
        path: 'perfil',
        component: Perfil
      },

      {
  path: 'perfil/editar',
  component: EditarPerfil
}
    ]
  },

  {
    path: '**',
    redirectTo: 'login'
  }

];