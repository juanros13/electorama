import { AccessService } from '@/app/core/access/access.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';

/**
 * El rol del token no tiene ninguna vista asignada todavía.
 *
 * Durante la reconstrucción del front esto va a pasar seguido, así que la
 * pantalla dice qué roles trae el token: sin eso, "no tienes acceso" obliga a
 * abrir las herramientas del navegador para saber por qué.
 */
@Component({
  selector: 'app-no-access',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [PageHeader, SectionCard],
  template: `
    <div class="flex min-w-0 flex-auto flex-col gap-6 p-6 sm:p-10">
      <app-page-header
        eyebrow="Acceso"
        title="Sin vistas asignadas"
        subtitle="Tu rol todavía no tiene ninguna vista del front nuevo. Se van abriendo conforme se rehacen."
      />

      <app-section-card heading="Roles de tu sesión">
        @if (roles().length) {
          <div class="flex flex-wrap gap-2">
            @for (role of roles(); track role) {
              <span
                class="rounded-full border border-neutral-200 px-3 py-1 font-mono text-xs dark:border-neutral-800"
                >{{ role }}</span
              >
            }
          </div>
        } @else {
          <p class="text-sm text-neutral-500">
            El token no trae roles. Vuelve a iniciar sesión.
          </p>
        }
      </app-section-card>
    </div>
  `,
})
export default class NoAccess {
  private access = inject(AccessService);
  protected roles = this.access.roles;
}
