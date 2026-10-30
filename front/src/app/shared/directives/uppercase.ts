import { Directive, ElementRef, HostListener, inject } from '@angular/core';
import { NgControl } from '@angular/forms';

/**
 * Fuerza mayúsculas en el input mientras se escribe y sincroniza el valor con
 * el control reactivo / ngModel. Reemplaza la directiva `appOnlyTypeUppercase`
 * del A15. Uso: `<input matInput appUppercase>`.
 */
@Directive({
  selector: '[appUppercase]',
})
export class UppercaseDirective {
  private el = inject<ElementRef<HTMLInputElement | HTMLTextAreaElement>>(ElementRef);
  private control = inject(NgControl, { optional: true });

  @HostListener('input')
  onInput(): void {
    const input = this.el.nativeElement;
    const upper = input.value.toUpperCase();
    if (input.value === upper) {
      return;
    }
    const start = input.selectionStart;
    const end = input.selectionEnd;
    if (this.control?.control) {
      this.control.control.setValue(upper, { emitEvent: false });
    }
    input.value = upper;
    // Preserva la posición del cursor tras reescribir el valor.
    if (start !== null && end !== null) {
      input.setSelectionRange(start, end);
    }
  }
}
