import { Component, inject } from '@angular/core';
import {
  Router,
  RouterLink,
  RouterLinkActive,
  RouterOutlet
} from '@angular/router';

@Component({
  selector: 'app-panel-cliente',
  imports: [
    RouterOutlet,
    RouterLink,
    RouterLinkActive
  ],
  templateUrl: './panel-cliente.html',
  styleUrl: './panel-cliente.css'
})
export class PanelCliente {

  private router = inject(Router);

  cerrarSesion(): void {
    sessionStorage.removeItem('cuentix_usuario');
    this.router.navigate(['/login']);
  }
}
