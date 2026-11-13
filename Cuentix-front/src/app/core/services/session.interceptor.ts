import { HttpInterceptorFn } from '@angular/common/http';
export const sessionInterceptor: HttpInterceptorFn = (request, next) => {
  if (!request.url.startsWith('/api/')) return next(request);
  let token = '';
  try {
    token = JSON.parse(sessionStorage.getItem('cuentix_usuario') ?? '{}').csrfToken ?? '';
  } catch {}
  return next(
    request.clone({ withCredentials: true, setHeaders: token ? { 'X-Cuentix-CSRF': token } : {} }),
  );
};
