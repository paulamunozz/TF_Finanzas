import { Component } from '@angular/core';

interface FilaReporte {
  id: number;
  negocio: string;
  cuota: string;
  mora: string;
  tieneMora: boolean;
  totalPagar: number;
  fechaVencimiento: string;
}

@Component({
  selector: 'app-reporte',
  imports: [],
  templateUrl: './reporte.html',
  styleUrl: './reporte.css'
})
export class Reporte {

  // Se utilizará cuando integremos los endpoints
  // específicos del cliente.
  filas: FilaReporte[] = [];

  cargando = false;
  error = '';

  get totalPagar(): number {
    return this.filas.reduce(
      (total, fila) => total + fila.totalPagar,
      0
    );
  }
}
