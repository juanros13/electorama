import { AuthUtils } from '@/app/core/auth/auth.utils';
import { AppConfigService } from '@/app/core/config/app-config';
import { NavigationItem } from '@/app/domains/admin/layout/data/navigation';
import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { catchError, map, Observable, of, tap } from 'rxjs';
import { AccessView, ViewId, viewsForRoles, VIEWS } from './access';

/** Item del menú tal como lo devuelve msvc-users (`getMenuByRol`). */
interface BackendMenuItem {
  id?: string;
  title?: string | null;
  subtitle?: string | null;
  icon?: string | null;
  link?: string | null;
  type?: string;
  order?: number | null;
}

/**
 * Los roles del token y las vistas que abren.
 *
 * Manda `msvc-users`: el menú y sus roles viven en su base y se administran
 * desde la pantalla de accesos. El mapa de `access.ts` queda de respaldo para
 * dos casos concretos —el servicio caído y la base sin sembrar— porque
 * quedarse sin menú por un servicio que no respondió es peor que enseñar el
 * que está en código.
 *
 * El catálogo de `access.ts` manda en otra cosa: qué rutas existen. Una entrada
 * de la base que apunte a una ruta que ya no está NO se muestra; la pantalla de
 * accesos la marca como enlace roto para poder reapuntarla.
 */
@Injectable({ providedIn: 'root' })
export class AccessService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  /** Los roles salen del token, el mismo dato que autoriza cada servicio. */
  private _roles = signal<string[]>(readRoles());
  readonly roles = this._roles.asReadonly();

  /** Vistas resueltas desde la base. `null` mientras no haya respuesta útil. */
  private _fromBackend = signal<AccessView[] | null>(null);

  /** De dónde salió lo que se está enseñando. La pantalla de accesos lo dice. */
  readonly source = computed<'backend' | 'codigo'>(() =>
    this._fromBackend() ? 'backend' : 'codigo'
  );

  readonly views = computed<AccessView[]>(
    () => this._fromBackend() ?? viewsForRoles(this._roles())
  );

  readonly navigation = computed<NavigationItem[]>(() =>
    this.views().map((view) => ({
      id: view.id,
      label: view.label,
      description: view.description,
      route: view.route,
      icon: view.icon,
    }))
  );

  readonly isAdmin = computed(() => this._roles().includes('ADMIN'));

  /**
   * Relee el token y vuelve a pedir el menú. Se llama al entrar, al renovar la
   * sesión y al salir.
   */
  refresh(): void {
    this._roles.set(readRoles());
    this.loadMenu().subscribe();
  }

  /**
   * Trae el menú que le toca a esta sesión.
   *
   * Se queda con las entradas cuya ruta existe de verdad en el front y las
   * ordena. Si no queda ninguna —servicio caído, sin token, base sin sembrar—
   * deja el respaldo en código en vez de dejar a la persona sin menú.
   */
  loadMenu(): Observable<AccessView[] | null> {
    if (!readRoles().length) {
      this._fromBackend.set(null);
      return of(null);
    }
    const url = `${this.appConfig.config.url_user}/menu/v1.0.0/getMenuByRol`;

    return this.http.get<BackendMenuItem[]>(url).pipe(
      map((items) => this.toViews(items ?? [])),
      catchError(() => of(null)),
      tap((views) => this._fromBackend.set(views))
    );
  }

  can(view: ViewId): boolean {
    return this.views().some((v) => v.id === view);
  }

  /** Entradas de la base → vistas del catálogo, descartando rutas que no existen. */
  private toViews(items: BackendMenuItem[]): AccessView[] | null {
    const byRoute = new Map(VIEWS.map((view) => [view.route, view]));

    const resolved = items
      .filter((item) => item?.link && byRoute.has(item.link.trim()))
      .map((item) => {
        const view = byRoute.get(item.link!.trim())!;
        return {
          ...view,
          // el título y el icono los manda la base: son presentación
          label: item.title?.trim() || view.label,
          icon: item.icon?.trim() || view.icon,
          order: item.order ?? 0,
        };
      })
      .sort((a, b) => a.order - b.order)
      .map(({ order, ...view }) => view as AccessView);

    return resolved.length ? resolved : null;
  }
}

/**
 * Roles del token guardado.
 *
 * Devuelve lista vacía ante cualquier problema —sin token, token roto, sin
 * `realm_access`— porque el caso por defecto tiene que ser "no ve nada", no
 * "ve todo".
 */
function readRoles(): string[] {
  try {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      return [];
    }
    const payload = AuthUtils.decodeToken(token);
    const roles = payload?.realm_access?.roles;
    return Array.isArray(roles) ? roles : [];
  } catch {
    return [];
  }
}
