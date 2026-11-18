import { ChangeDetectionStrategy, Component, signal, inject } from '@angular/core';
import { BusinessContext } from '../../core/services/business-context.service';
import { AuthService } from '../../core/services/auth.service';
import { Router } from '@angular/router';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-main-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MainLayout {
  protected readonly business = inject(BusinessContext);
  private auth = inject(AuthService);
  private router = inject(Router);
  protected logout() {
    this.auth
      .logout()
      .subscribe({
        next: () => this.router.navigate(['/auth/login']),
        error: () => this.business.error.set('No se pudo cerrar la sesión.'),
      });
  }
  protected readonly collapsed = signal(false);
  protected readonly menuOpen = signal(false);
  protected toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }
  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
}
