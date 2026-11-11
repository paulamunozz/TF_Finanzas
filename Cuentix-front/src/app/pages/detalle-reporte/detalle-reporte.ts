import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

interface CuotaReporte {
  id: number;
  fechaCompra: string;
  numeroCuota: string;
  mora: string;
  tieneMora: boolean;
  totalPagar: number;
  fechaVencimiento: string;
}

@Component({
  selector: 'app-detalle-reporte',
  imports: [RouterLink],
  templateUrl: './detalle-reporte.html',
  styleUrl: './detalle-reporte.css'
})
export class DetalleReporte {

  // Nombre de la tienda seleccionada.
  // Se obtendrá cuando conectemos el backend.
  nombreTienda = '';

  // Cuotas correspondientes a la tienda.
  cuotas: CuotaReporte[] = [];

  cargando = false;
  error = '';

  get totalPagar(): number {
    return this.cuotas.reduce(
      (total, cuota) => total + cuota.totalPagar,
      0
    );
  }
}