import { NavService } from '@/app/core/navigation/nav.service';
import { inject } from '@angular/core';
import { CanActivateFn, Router, UrlTree } from '@angular/router';

/**
 * Si hay un flag de "navegar a settings" pendiente, fuerza ir a /admin/settings
 * una sola vez (p.ej. coordinador que debe completar sus datos). Portado del
 * NavGuard viejo.
 */
export const navGuard: CanActivateFn = (): boolean | UrlTree => {
    const navService = inject(NavService);
    const router = inject(Router);

    if (navService.getNavSettings()) {
        navService.setNavSettings('false');
        return router.parseUrl('/admin/settings');
    }
    return true;
};
