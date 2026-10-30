import { AuthService } from '@/app/core/auth/auth.service';
import { inject } from '@angular/core';
import { CanMatchFn, Router, UrlTree } from '@angular/router';
import { map, Observable } from 'rxjs';

/**
 * Permite la ruta solo si hay sesión válida; si no, redirige a sign-in
 * conservando la URL destino en `redirectURL`.
 */
export const authGuard: CanMatchFn = (route, segments) => {
    const authService = inject(AuthService);
    const router = inject(Router);

    return authService.check().pipe(
        map((authenticated): boolean | UrlTree => {
            if (!authenticated) {
                const redirectURL = `/${segments.join('/')}`;
                return router.parseUrl(`/auth/sign-in?redirectURL=${redirectURL}`);
            }
            return true;
        })
    );
};

/** Permite la ruta solo si NO hay sesión (páginas de auth). */
export const noAuthGuard: CanMatchFn = (): Observable<boolean> => {
    const authService = inject(AuthService);
    return authService.check().pipe(map((authenticated) => !authenticated));
};
