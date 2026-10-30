import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Encabezado de página estándar (cívica enérgica): barra de acento bermellón +
 * título en fuente display + subtítulo opcional + slot de acciones.
 *
 * Uso:
 *   <app-page-header eyebrow="Censo 2024" title="Lista de censos" subtitle="...">
 *     <button actions matButton="filled">…</button>
 *   </app-page-header>
 */
@Component({
  selector: 'app-page-header',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    <header
      class="anim-rise flex flex-col gap-4 border-b border-neutral-200 pb-5 sm:flex-row sm:items-end sm:justify-between dark:border-neutral-800"
    >
      <div class="flex items-start gap-3">
        <span class="mt-1 h-9 w-1.5 shrink-0 rounded-full bg-primary-600"></span>
        <div class="min-w-0">
          @if (eyebrow()) {
            <div
              class="text-xs font-semibold tracking-wider text-secondary-700 uppercase dark:text-secondary-400"
            >
              {{ eyebrow() }}
            </div>
          }
          <h1 class="font-display text-3xl font-bold tracking-tight sm:text-4xl">
            {{ title() }}
          </h1>
          @if (subtitle()) {
            <p class="mt-1 text-neutral-500">{{ subtitle() }}</p>
          }
        </div>
      </div>
      <div class="flex flex-wrap items-center gap-2">
        <ng-content select="[actions]" />
      </div>
    </header>
  `,
})
export class PageHeader {
  eyebrow = input('');
  title = input('');
  subtitle = input('');
}
