import { httpResource } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { API_BASE_URL } from '../config/api.config';

interface CatalogOption {
  id: number;
  nombre: string;
}

interface CurrencyOption extends CatalogOption {
  simbolo: string;
  code: string;
}

@Injectable({ providedIn: 'root' })
export class ReferenceCatalogService {
  private readonly api = inject(API_BASE_URL);

  currencies() {
    return httpResource<CurrencyOption[]>(() => `${this.api}/catalog/currencies`, { defaultValue: [] });
  }

  units(enabled: () => boolean) {
    return httpResource<CatalogOption[]>(() => enabled() ? `${this.api}/catalog/units` : undefined, {
      defaultValue: [],
    });
  }

  sectors() {
    return httpResource<CatalogOption[]>(() => `${this.api}/admin/businesses/giros`, { defaultValue: [] });
  }
}
