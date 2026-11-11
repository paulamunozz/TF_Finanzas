import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface ProductoComprado {
  id: number;
  nombre: string;
  cantidad: number;
  unidad: string;
}

interface CuotaDetalle {
  id: number;
  fechaCompra: string;
  numeroCuota: string;
  fechaVencimiento: string;
  interes: number;
  amortizacion: number;
  cuota: number;
  mora: number;
  cancelado: boolean;
  totalPagar: number;
  productos: ProductoComprado[];
}

@Component({
  selector: 'app-detalle-cuota',
  imports: [RouterLink],
  templateUrl: './detalle-cuota.html',
  styleUrl: './detalle-cuota.css'
})
export class DetalleCuota {

  // Se obtendrá del backend del cliente.
  nombreTienda = '';

  // Datos pendientes de cargar.
  detalle: CuotaDetalle | null = null;

  cargando = false;
  error = '';

}