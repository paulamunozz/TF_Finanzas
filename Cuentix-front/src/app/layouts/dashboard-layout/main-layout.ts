import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-main-layout',
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './main-layout.html',
  styleUrl: './main-layout.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class MainLayout {
  protected readonly collapsed = signal(false);
  protected readonly menuOpen = signal(false);
  protected toggleMenu(): void {
    this.menuOpen.update((value) => !value);
  }
  protected closeMenu(): void {
    this.menuOpen.set(false);
  }
}
