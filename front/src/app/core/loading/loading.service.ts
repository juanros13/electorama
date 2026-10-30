import { DestroyRef, inject, Injectable, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  NavigationCancel,
  NavigationEnd,
  NavigationError,
  NavigationStart,
  Router,
} from '@angular/router';
import { finalize, Observable } from 'rxjs';

/** Lo que tiene que tardar algo para que se vea el cargador: evita el parpadeo. */
const SHOW_AFTER_MS = 150;

/**
 * El cargador global: cuenta el trabajo en curso y dice si hay que enseñarlo.
 *
 * Lo alimentan solos el interceptor HTTP (cada petición) y la navegación del
 * router (la carga de rutas perezosas). Para trabajo que no es HTTP —un cálculo,
 * una promesa de un SDK— se usa {@link track} o {@link start}.
 *
 * Es un contador, no un booleano: con tres peticiones en paralelo el cargador
 * sigue visible hasta que termina la última.
 */
@Injectable({ providedIn: 'root' })
export class LoadingService {
  private readonly pending = signal(0);
  private readonly visible = signal(false);
  private timer: ReturnType<typeof setTimeout> | null = null;

  /** Hay algo en curso desde hace más de {@link SHOW_AFTER_MS}. */
  readonly active = this.visible.asReadonly();

  constructor() {
    const router = inject(Router);
    // una navegación abierta por ruta: NavigationStart abre, cualquier final cierra
    let navigating = false;
    router.events
      .pipe(takeUntilDestroyed(inject(DestroyRef)))
      .subscribe((event) => {
        if (event instanceof NavigationStart && !navigating) {
          navigating = true;
          this.start();
        } else if (
          navigating &&
          (event instanceof NavigationEnd ||
            event instanceof NavigationCancel ||
            event instanceof NavigationError)
        ) {
          navigating = false;
          this.stop();
        }
      });
  }

  /** Abre un trabajo. Cada `start()` necesita su `stop()`. */
  start(): void {
    this.pending.update((n) => n + 1);
    if (this.timer === null && !this.visible()) {
      this.timer = setTimeout(() => {
        this.timer = null;
        if (this.pending() > 0) {
          this.visible.set(true);
        }
      }, SHOW_AFTER_MS);
    }
  }

  /** Cierra un trabajo; con el último, el cargador se va. */
  stop(): void {
    this.pending.update((n) => Math.max(0, n - 1));
    if (this.pending() === 0) {
      if (this.timer !== null) {
        clearTimeout(this.timer);
        this.timer = null;
      }
      this.visible.set(false);
    }
  }

  /** Enseña el cargador mientras dure el observable (termine bien, mal o se cancele). */
  track<T>(source: Observable<T>): Observable<T> {
    return new Observable<T>((subscriber) => {
      this.start();
      return source.pipe(finalize(() => this.stop())).subscribe(subscriber);
    });
  }

  /** Lo mismo para una promesa. */
  async trackPromise<T>(work: Promise<T>): Promise<T> {
    this.start();
    try {
      return await work;
    } finally {
      this.stop();
    }
  }
}
