import { TestBed } from '@angular/core/testing';
import { provideHttpClient, withInterceptors, HttpClient } from '@angular/common/http';
import { provideHttpClientTesting, HttpTestingController } from '@angular/common/http/testing';
import { provideRouter, Router } from '@angular/router';
import { ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { firstValueFrom, Observable } from 'rxjs';
import { GuardResult, UrlTree } from '@angular/router';
import { sessionInterceptor } from './session.interceptor';
import { sessionGuard } from './session.guard';
import { AuthService } from './auth.service';
describe('Server session and CSRF', () => {
  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideRouter([]),
        provideHttpClient(withInterceptors([sessionInterceptor])),
        provideHttpClientTesting(),
        AuthService,
      ],
    });
  });
  afterEach(() => TestBed.inject(HttpTestingController).verify());
  it('attaches credentials and CSRF only to relative API requests', () => {
    sessionStorage.setItem('cuentix_usuario', JSON.stringify({ csrfToken: 'test-token' }));
    TestBed.inject(HttpClient).put('/api/cliente/mi-perfil', {}).subscribe();
    const req = TestBed.inject(HttpTestingController).expectOne('/api/cliente/mi-perfil');
    expect(req.request.withCredentials).toBe(true);
    expect(req.request.headers.get('X-Cuentix-CSRF')).toBe('test-token');
    req.flush({});
    TestBed.inject(HttpClient).get('https://example.invalid/api').subscribe();
    const remote = TestBed.inject(HttpTestingController).expectOne('https://example.invalid/api');
    expect(remote.request.headers.has('X-Cuentix-CSRF')).toBe(false);
    remote.flush({});
  });
  it('checks the server even when local session claims an authorized role', async () => {
    sessionStorage.setItem('cuentix_usuario', JSON.stringify({ rolId: 2, csrfToken: 'forged' }));
    const route = { data: { roles: [2] } } as unknown as ActivatedRouteSnapshot;
    const result = TestBed.runInInjectionContext(() =>
      sessionGuard(route, {} as RouterStateSnapshot),
    );
    const resolved = firstValueFrom(result as Observable<GuardResult>);
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/session')
      .flush({ message: 'Expired' }, { status: 401, statusText: 'Unauthorized' });
    expect(TestBed.inject(Router).serializeUrl((await resolved) as UrlTree)).toBe('/auth/login');
    expect(sessionStorage.getItem('cuentix_usuario')).toBeNull();
  });
  it('restores current profile and session token from the server', () => {
    TestBed.inject(AuthService).recuperar().subscribe();
    TestBed.inject(HttpTestingController)
      .expectOne('/api/auth/session')
      .flush({ id: 2, rolId: 2, csrfToken: 'current', codigo: '12345678' });
    expect(JSON.parse(sessionStorage.getItem('cuentix_usuario')!).csrfToken).toBe('current');
  });
});
