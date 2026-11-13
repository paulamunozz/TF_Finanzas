import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
@Component({
  selector: 'app-panel-cliente',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './panel-cliente.html',
  styleUrl: './panel-cliente.css',
})
export class PanelCliente {}
