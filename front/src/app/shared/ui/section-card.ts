import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Contenedor de sección de formulario: borde redondeado + título opcional.
 *
 * Uso:
 *   <app-section-card heading="Datos personales">
 *     <div class="grid …">…</div>
 *   </app-section-card>
 */
@Component({
  selector: 'app-section-card',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    <section class="rounded-xl border border-neutral-200 p-5 dark:border-neutral-800">
      @if (heading()) {
        <h3 class="mb-4 font-display text-lg font-semibold tracking-tight">{{ heading() }}</h3>
      }
      <ng-content />
    </section>
  `,
})
export class SectionCard {
  heading = input('');
}
