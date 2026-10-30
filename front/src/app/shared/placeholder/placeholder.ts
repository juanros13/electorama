import { Component, inject } from '@angular/core';
import { MatIcon } from '@angular/material/icon';
import { ActivatedRoute } from '@angular/router';

/**
 * Placeholder para módulos del proyecto viejo aún no portados. Permite que el
 * shell (login + layout + menú) sea navegable durante la migración: cualquier
 * ruta de módulo no migrada muestra este estado en vez de un 404.
 */
@Component({
  selector: 'app-placeholder',
  imports: [MatIcon],
  template: `
    <div
      class="flex flex-auto flex-col items-center justify-center gap-y-4 p-12 text-center"
    >
      <mat-icon
        class="size-16 text-neutral-400"
        svgIcon="construction"
      />
      <div class="text-2xl font-semibold">Módulo en migración</div>
      <div class="max-w-md text-neutral-500">
        Esta sección (<span class="font-mono">{{ path() }}</span>) todavía no se
        ha portado a la nueva versión. Estará disponible conforme avance la
        migración.
      </div>
    </div>
  `,
})
export default class Placeholder {
  private route = inject(ActivatedRoute);

  protected path(): string {
    return '/' + this.route.snapshot.url.map((s) => s.path).join('/');
  }
}
