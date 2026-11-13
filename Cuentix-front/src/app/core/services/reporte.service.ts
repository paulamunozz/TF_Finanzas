import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
export interface ProductoComprado {
  id: number;
  nombre: string;
  cantidad: number;
  unidad: string;
}
export interface CuotaCliente {
  id: number;
  numero: number;
  fechaVencimiento: string;
  interes: number;
  amortizacion: number;
  mora: number;
  total: number;
  cancelado: boolean;
}
export interface CompraCliente {
  id: number;
  cuentaId: number;
  tienda: string;
  moneda: string;
  fecha: string | null;
  total: number | null;
  meses: number;
}
export interface DetalleCompra extends CompraCliente {
  productos: ProductoComprado[];
  cuotas: CuotaCliente[];
}
@Injectable({ providedIn: 'root' })
export class ReporteService {
  private http = inject(HttpClient);
  compras() {
    return this.http.get<CompraCliente[]>('/api/cliente/mis-compras');
  }
  detalle(id: string) {
    return this.http.get<DetalleCompra>('/api/cliente/mis-compras/' + encodeURIComponent(id));
  }
}
