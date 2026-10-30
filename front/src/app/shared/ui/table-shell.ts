import { ChangeDetectionStrategy, Component, input } from '@angular/core';

/**
 * Contenedor estándar de tablas: borde redondeado + barra de carga temada
 * (bermellón) + estado vacío. Proyecta la `<table mat-table>` adentro.
 *
 * Uso:
 *   <app-table-shell [loading]="loading()" [empty]="!data().length" emptyText="Sin resultados.">
 *     <table mat-table …></table>
 *   </app-table-shell>
 */
@Component({
  selector: 'app-table-shell',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'block' },
  template: `
    <div
      class="relative overflow-auto rounded-xl border border-neutral-200 dark:border-neutral-800"
    >
      @if (loading()) {
        <div class="absolute inset-x-0 top-0 z-10 h-1 animate-pulse bg-primary-500"></div>
      }
      <ng-content />
      @if (empty() && !loading()) {
        <div class="p-8 text-center text-neutral-500">{{ emptyText() }}</div>
      }
    </div>
  `,
})
export class TableShell {
  loading = input(false);
  empty = input(false);
  emptyText = input('No hay registros para mostrar.');
}
