import { InjectionToken } from '@angular/core';

/** Same-origin API implemented by Spring; the development proxy forwards /api. */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => '/api',
});
