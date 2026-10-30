import { inject } from '@angular/core';
import { CanMatchFn, Router } from '@angular/router';
import { ViewId } from './access';
import { AccessService } from './access.service';

/**
 * Deja pasar sólo si el rol del token abre esa vista.
 *
 * Va como `canMatch` y no `canActivate` a propósito: así Angular ni siquiera
 * descarga el chunk de una vista que el rol no ve.
 *
 * Quien no la tiene va a la primera vista que sí le toque, y si no le toca
 * ninguna, a la pantalla que se lo explica. Mandarlo a un 404 sería mentirle:
 * la ruta existe, lo que no tiene es acceso.
 */
export function canSee(view: ViewId): CanMatchFn {
  return () => {
    const access = inject(AccessService);
    const router = inject(Router);

    if (access.can(view)) {
      return true;
    }
    const fallback = access.views()[0];
    return router.parseUrl(fallback ? fallback.route : '/admin/sin-acceso');
  };
}
