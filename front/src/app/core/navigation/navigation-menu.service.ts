import { AppConfigService } from '@/app/core/config/app-config';
import { NavigationItem } from '@/app/domains/admin/layout/data/navigation';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { catchError, of, tap } from 'rxjs';

/** Forma (parcial) del item de menú que devuelve el backend (estilo Fuse). */
interface BackendMenuItem {
  pk?: number | null;
  id?: string;
  title?: string | null;
  subtitle?: string | null;
  type?: string; // 'group' | 'basic' | 'collapsable' | 'aside' | 'divider'
  icon?: string | null; // p.ej. 'heroicons_outline:home'
  link?: string | null;
  children?: BackendMenuItem[] | null;
}

/** Mapeo best-effort de iconos heroicons → lucide (ids del set registrado). */
const ICON_MAP: Record<string, string> = {
  home: 'house',
  'chart-pie': 'chart-pie',
  map: 'map',
  calendar: 'calendar',
  'bookmark-alt': 'bookmark',
  users: 'users',
  'user-group': 'users',
  'user-add': 'user-plus',
  'people-check': 'user-round-check',
  identification: 'id-card',
  document: 'file-text',
  'document-text': 'file-text',
  'clipboard-list': 'clipboard-list',
  'location-marker': 'map-pin',
  cog: 'settings',
  'view-grid': 'layout-grid',
};

/**
 * Menú de navegación dirigido por backend (`GET {url_user}/menu/v1.0.0/getMenuByRol`,
 * el rol se deriva del token). Mapea la respuesta al `NavigationItem` del theme
 * y la expone como signal. Si la petición falla (offline/sin token), usa un
 * menú de campaña estático como fallback para que el shell sea navegable.
 *
 * NOTE: la forma exacta de la respuesta y los nombres de icono del backend no se
 * pudieron verificar contra el API en vivo; el mapeo es defensivo.
 */
@Injectable({ providedIn: 'root' })
export class NavigationMenuService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private readonly _navigation = signal<NavigationItem[]>(FALLBACK_NAVIGATION);
  readonly navigation = this._navigation.asReadonly();

  /** Carga el menú desde el backend; deja el fallback si falla. */
  load(): void {
    const url = `${this.appConfig.config.url_user}/menu/v1.0.0/getMenuByRol`;
    this.http
      .get<BackendMenuItem[]>(url)
      .pipe(
        catchError(() => of(null)),
        tap((items) => {
          const mapped = items ? this.mapItems(items) : [];
          if (mapped.length) {
            this._navigation.set(mapped);
          }
        })
      )
      .subscribe();
  }

  private mapItems(items: BackendMenuItem[]): NavigationItem[] {
    return items
      .filter((item) => item.type !== 'divider')
      .map((item) => this.mapItem(item))
      .filter((item): item is NavigationItem => item !== null);
  }

  private mapItem(item: BackendMenuItem): NavigationItem | null {
    if (item.type === 'divider') {
      return null;
    }

    const children = item.children ? this.mapItems(item.children) : undefined;

    return {
      // `pk` es único; evita claves duplicadas en @for (el backend repite `id`).
      id: String(item.pk ?? item.id ?? crypto.randomUUID()),
      label: item.title ?? '',
      description: item.subtitle ?? undefined,
      route: item.link ? this.prefixRoute(item.link) : undefined,
      icon: this.mapIcon(item.icon),
      children: children?.length ? children : undefined,
    };
  }

  /** Las rutas del backend no traen el prefijo `/admin` del nuevo routing. */
  private prefixRoute(link: string): string {
    if (!link.startsWith('/')) {
      return link;
    }
    return link.startsWith('/admin') ? link : `/admin${link}`;
  }

  private mapIcon(icon?: string | null): string | undefined {
    if (!icon) {
      return undefined;
    }
    const name = icon.includes(':') ? icon.split(':').pop()! : icon;
    return ICON_MAP[name] ?? name;
  }
}

/** Menú de campaña estático usado como fallback. Rutas → placeholder hasta portar. */
const FALLBACK_NAVIGATION: NavigationItem[] = [
  {
    id: 'inicio',
    label: 'Inicio',
    children: [
      {
        id: 'dashboards',
        label: 'Tablero',
        icon: 'house',
        route: '/admin/dashboards',
      },
    ],
  },
  {
    id: 'territorio',
    label: 'Territorio',
    description: 'Estructura y personas',
    children: [
      {
        id: 'follower',
        label: 'Simpatizantes',
        icon: 'users',
        route: '/admin/follower',
      },
      {
        id: 'promoters',
        label: 'Movilizadores',
        icon: 'user-round-check',
        route: '/admin/promoters',
      },
      {
        id: 'structure',
        label: 'Estructura',
        icon: 'network',
        route: '/admin/structure/config',
      },
      {
        id: 'maps',
        label: 'Mapas electorales',
        icon: 'map',
        route: '/admin/maps/nacional',
      },
    ],
  },
  {
    id: 'operacion',
    label: 'Operación',
    description: 'Actividades en territorio',
    children: [
      {
        id: 'visit',
        label: 'Visitas',
        icon: 'footprints',
        route: '/admin/visit',
      },
      {
        id: 'canvas-walls',
        label: 'Lienzos y murales',
        icon: 'image',
        route: '/admin/canvas-walls',
      },
      {
        id: 'surveys',
        label: 'Encuestas',
        icon: 'clipboard-list',
        route: '/admin/surveys',
      },
      {
        id: 'citizen-issues',
        label: 'Problemas ciudadanos',
        icon: 'megaphone',
        route: '/admin/citizen-issues',
      },
      {
        id: 'safehouse',
        label: 'Casa segura',
        icon: 'house-plus',
        route: '/admin/safehouse',
      },
      {
        id: 'agenda',
        label: 'Agenda electoral',
        icon: 'calendar',
        route: '/admin/agenda',
      },
    ],
  },
  {
    id: 'jornada',
    label: 'Jornada',
    description: 'Día de la elección',
    children: [
      {
        id: 'mobilization',
        label: 'Movilización',
        icon: 'route',
        route: '/admin/mobilization',
      },
      {
        id: 'defense-vote',
        label: 'Defensa de voto',
        icon: 'shield-check',
        route: '/admin/defense-vote',
      },
      {
        id: 'elections',
        label: 'Elecciones',
        icon: 'vote',
        route: '/admin/elections',
      },
    ],
  },
];
