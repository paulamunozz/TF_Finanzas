import { InjectionToken, isDevMode } from '@angular/core';

// Cambia a false para volver a exigir inicio de sesion en desarrollo.
// Mientras sea true, las rutas de cliente y negocio quedan sin guards.
export const PREVIEW_WITHOUT_LOGIN = true;
export const AUTH_PREVIEW = new InjectionToken<boolean>('AUTH_PREVIEW', {
  providedIn: 'root',
  factory: () => isDevMode() && PREVIEW_WITHOUT_LOGIN,
});
