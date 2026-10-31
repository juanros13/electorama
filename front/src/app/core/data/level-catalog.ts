import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, of } from 'rxjs';
import { functionName, LevelLike, StructureLevel } from './levels';

/**
 * Los niveles de la campaña activa, para las pantallas que nombran un nivel por
 * su función (captura, convencidos) sin cargar la campaña por su cuenta. Se
 * pide una vez; mientras no llega, los nombres son los de respaldo.
 */
@Injectable({ providedIn: 'root' })
export class LevelCatalog {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private requested = false;

  readonly levels = signal<LevelLike[]>([]);

  /** Pide los niveles de la campaña activa (sólo la primera vez). */
  load(): void {
    if (this.requested) {
      return;
    }
    this.requested = true;
    this.http
      .get<{ levels: LevelLike[] }>(`${this.appConfig.config.url_structure}/campaigns/active`)
      .pipe(catchError(() => of({ levels: [] as LevelLike[] })))
      .subscribe((c) => this.levels.set(c.levels ?? []));
  }

  /** Para que una pantalla recargue después de configurar la campaña. */
  set(levels: LevelLike[]): void {
    this.requested = true;
    this.levels.set(levels);
  }

  name(fn: StructureLevel | null | undefined, plural = false): string {
    return functionName(this.levels(), fn, plural);
  }
}
