import { HttpContext, HttpContextToken, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { finalize } from 'rxjs';
import { LoadingService } from './loading.service';

/**
 * Marca una petición para que no encienda el cargador global. Para lo que corre
 * en segundo plano y el usuario no está esperando: bitácora, sondeos, prefetch.
 *
 * ```ts
 * this.http.get(url, { context: skipLoading() })
 * ```
 */
export const SKIP_LOADING = new HttpContextToken<boolean>(() => false);

export const skipLoading = (context = new HttpContext()): HttpContext =>
  context.set(SKIP_LOADING, true);

/**
 * Cada petición HTTP cuenta en el cargador global mientras está en vuelo.
 *
 * La bitácora (`logfile`) y la IP pública se omiten solas: se disparan en cada
 * pantalla y nadie las espera.
 */
export const loadingInterceptor: HttpInterceptorFn = (req, next) => {
  if (
    req.context.get(SKIP_LOADING) ||
    req.url.includes('logfile') ||
    req.url.startsWith('https://api64.ipify.org')
  ) {
    return next(req);
  }

  const loading = inject(LoadingService);
  loading.start();
  return next(req).pipe(finalize(() => loading.stop()));
};
