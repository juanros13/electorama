import { AuthService } from '@/app/core/auth/auth.service';
import { AuthUtils } from '@/app/core/auth/auth.utils';
import { AppConfigService } from '@/app/core/config/app-config';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';

/**
 * Añade el header Authorization (Bearer) cuando hay token válido y cierra la
 * sesión ante un 401 (salvo en el propio endpoint de token de Keycloak).
 *
 * Se omiten: ipify (IP service), logfile y GeoServer (este último sin auth tras
 * eliminar `geoserver_key` — ver inventario de secrets).
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const authService = inject(AuthService);
    const config = inject(AppConfigService).config$();
    // el host de GeoServer sale de config.json (url_geoserver / url_geoserver_ows)
    const isGeoServer = (url: string) =>
        [config?.url_geoserver, config?.url_geoserver_ows].some(
            (base) => !!base && url.startsWith(new URL(base).origin)
        );

    if (
        req.url.startsWith('https://api64.ipify.org') ||
        req.url.includes('logfile') ||
        isGeoServer(req.url)
    ) {
        return next(req);
    }

    let authReq = req;
    const token = authService.accessToken;
    if (token && !AuthUtils.isTokenExpired(token)) {
        authReq = req.clone({
            headers: req.headers.set('Authorization', `Bearer ${token}`),
        });
    }

    return next(authReq).pipe(
        catchError((error) => {
            if (
                error instanceof HttpErrorResponse &&
                error.status === 401 &&
                !error.url?.includes('/openid-connect/token')
            ) {
                authService.signOut();
                location.reload();
            }
            return throwError(() => error);
        })
    );
};
