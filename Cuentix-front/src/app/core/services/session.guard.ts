import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { catchError, map, of } from 'rxjs';
import { AuthService } from './auth.service';
export const sessionGuard: CanActivateFn = (route) => {
  const router = inject(Router);
  return inject(AuthService)
    .recuperar()
    .pipe(
      map((u) =>
        (route.data['roles'] as number[]).includes(u.rolId ?? 0)
          ? true
          : router.createUrlTree([u.rolId === 2 ? '/cliente/reporte' : '/dashboard']),
      ),
      catchError(() => {
        sessionStorage.removeItem('cuentix_usuario');
        return of(router.createUrlTree(['/auth/login']));
      }),
    );
};
