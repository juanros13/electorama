import { AuthService } from '@/app/core/auth/auth.service';
import { AuthUtils } from '@/app/core/auth/auth.utils';
import { AppConfigService } from '@/app/core/config/app-config';
import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, finalize, Observable, of, shareReplay, switchMap, throwError } from 'rxjs';

/** Segundos antes del vencimiento en que ya se renueva el token (Keycloak lo da por 5 minutos). */
const REFRESH_MARGIN_SECONDS = 30;

/** La renovación en curso: varias peticiones a la vez esperan la misma. */
let refreshing$: Observable<boolean> | null = null;

function refresh(authService: AuthService): Observable<boolean> {
    if (!refreshing$) {
        refreshing$ = authService.signInUsingToken().pipe(
            finalize(() => (refreshing$ = null)),
            shareReplay(1)
        );
    }
    return refreshing$;
}

/**
 * Añade el header Authorization (Bearer). Si el token está por vencer lo renueva antes con el
 * refresh token, y ante un 401 renueva y reintenta una vez; sólo si eso falla cierra la sesión.
 * Las llamadas al endpoint de token de Keycloak pasan sin tocar.
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
        req.url.includes('/openid-connect/token') ||
        isGeoServer(req.url)
    ) {
        return next(req);
    }

    const withToken = (r: HttpRequest<unknown>) => {
        const token = authService.accessToken;
        return token && !AuthUtils.isTokenExpired(token)
            ? r.clone({ headers: r.headers.set('Authorization', `Bearer ${token}`) })
            : r;
    };
    const signOut = (error: unknown) => {
        authService.signOut();
        location.reload();
        return throwError(() => error);
    };

    const token = authService.accessToken;
    const ready$ =
        token && authService.refreshToken && AuthUtils.isTokenExpired(token, REFRESH_MARGIN_SECONDS)
            ? refresh(authService)
            : of(true);

    return ready$.pipe(
        switchMap(() => next(withToken(req))),
        catchError((error) => {
            if (!(error instanceof HttpErrorResponse) || error.status !== 401) {
                return throwError(() => error);
            }
            if (!authService.refreshToken) {
                return signOut(error);
            }
            // un 401 con token vigente en el reloj local: se renueva y se reintenta una vez
            return refresh(authService).pipe(
                switchMap((ok) =>
                    ok
                        ? next(withToken(req)).pipe(
                              catchError((again) =>
                                  again instanceof HttpErrorResponse && again.status === 401
                                      ? signOut(again)
                                      : throwError(() => again)
                              )
                          )
                        : signOut(error)
                )
            );
        })
    );
};
