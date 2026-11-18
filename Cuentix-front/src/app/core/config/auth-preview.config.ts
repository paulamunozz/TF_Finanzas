import { InjectionToken, isDevMode } from '@angular/core';

export const PREVIEW_WITHOUT_LOGIN = false;
export const AUTH_PREVIEW = new InjectionToken<boolean>('AUTH_PREVIEW', {
  providedIn: 'root',
  factory: () => isDevMode() && PREVIEW_WITHOUT_LOGIN,
});
