import {
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  input,
  viewChild,
} from '@angular/core';

let nextTooltipId = 0;

@Component({
  selector: 'app-field-help',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './field-help.html',
  styleUrl: './field-help.scss',
})
export class FieldHelp {
  readonly field = input.required<string>();
  readonly message = input.required<string>();

  protected readonly tooltipId = 'field-help-' + nextTooltipId++;
  private readonly trigger = viewChild.required<ElementRef<HTMLButtonElement>>('trigger');
  private readonly tooltip = viewChild.required<ElementRef<HTMLElement>>('tooltip');
  private closeTimer?: ReturnType<typeof setTimeout>;
  private listeners?: AbortController;
  private clicked = false;

  constructor() {
    inject(DestroyRef).onDestroy(() => this.reset());
  }

  protected enter(event: PointerEvent): void {
    if (event.pointerType !== 'touch') this.show();
  }

  protected leave(event: PointerEvent): void {
    if (event.pointerType === 'touch' || this.trigger().nativeElement.matches(':focus-visible')) {
      return;
    }
    this.closeTimer = setTimeout(() => this.hide(), 160);
  }

  protected focus(): void {
    if (this.trigger().nativeElement.matches(':focus-visible')) this.show();
  }

  protected toggle(event: MouseEvent): void {
    event.preventDefault();
    if (this.clicked && this.tooltip().nativeElement.matches(':popover-open')) {
      this.hide();
      return;
    }
    this.show();
    this.clicked = true;
  }

  protected hide(): void {
    const tooltip = this.tooltip().nativeElement;
    if (tooltip.matches(':popover-open')) tooltip.hidePopover();
    this.reset();
  }

  protected syncPopover(): void {
    if (!this.tooltip().nativeElement.matches(':popover-open')) this.reset();
  }

  protected cancelClose(): void {
    clearTimeout(this.closeTimer);
  }

  private show(): void {
    this.cancelClose();
    const trigger = this.trigger().nativeElement;
    const tooltip = this.tooltip().nativeElement;
    if (tooltip.matches(':popover-open')) return;
    this.reset();

    tooltip.showPopover({ source: trigger });
    const viewport = trigger.ownerDocument.defaultView;
    this.listeners = new AbortController();
    const { signal } = this.listeners;
    viewport?.addEventListener('resize', this.position, { signal });
    viewport?.addEventListener('scroll', this.position, { capture: true, signal });
    viewport?.visualViewport?.addEventListener('resize', this.position, { signal });
    viewport?.visualViewport?.addEventListener('scroll', this.position, { signal });
    // Escape dismisses the help before a surrounding dialog handles the same key.
    viewport?.addEventListener('keydown', this.handleEscape, { capture: true, signal });
    this.position();
  }

  private readonly handleEscape = (event: KeyboardEvent): void => {
    if (event.key !== 'Escape' || !this.tooltip().nativeElement.matches(':popover-open')) return;
    event.preventDefault();
    event.stopPropagation();
    this.hide();
  };

  private readonly position = (): void => {
    const trigger = this.trigger().nativeElement;
    const tooltip = this.tooltip().nativeElement;
    if (!tooltip.matches(':popover-open')) return;
    const viewport = trigger.ownerDocument.defaultView?.visualViewport;
    const { clientWidth, clientHeight } = trigger.ownerDocument.documentElement;
    const left = viewport?.offsetLeft ?? 0;
    const top = viewport?.offsetTop ?? 0;
    const right = left + (viewport?.width ?? clientWidth);
    const bottom = top + (viewport?.height ?? clientHeight);
    const anchor = trigger.getBoundingClientRect();
    if (anchor.bottom <= top || anchor.top >= bottom || anchor.right <= left || anchor.left >= right) {
      this.hide();
      return;
    }

    const margin = 8;
    const gap = 5;
    const above = anchor.top - top - margin - gap;
    const below = bottom - anchor.bottom - margin - gap;
    tooltip.style.maxWidth = Math.min(240, right - left - 2 * margin) + 'px';
    tooltip.style.removeProperty('--field-help-max-height');
    const size = tooltip.getBoundingClientRect();
    const placeAbove = size.height <= above || above >= below;
    tooltip.style.setProperty('--field-help-max-height', Math.max(0, placeAbove ? above : below) + 'px');
    const x = Math.min(Math.max(anchor.left, left + margin), right - margin - size.width);
    const y = placeAbove ? anchor.top - gap - Math.min(size.height, above) : anchor.bottom + gap;
    tooltip.style.left = x + 'px';
    tooltip.style.top = y + 'px';
  };

  private reset(): void {
    this.cancelClose();
    this.listeners?.abort();
    this.listeners = undefined;
    this.clicked = false;
  }
}
