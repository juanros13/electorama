import { ROLES, VIEWS } from '@/app/core/access/access';
import { AccessService } from '@/app/core/access/access.service';
import {
  MenuAdminItem,
  MenuAdminService,
} from '@/app/core/access/menu-admin.service';
import { PageHeader } from '@/app/shared/ui/page-header';
import { SectionCard } from '@/app/shared/ui/section-card';
import { StatCard } from '@/app/shared/ui/stat-card';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTooltipModule } from '@angular/material/tooltip';
import { catchError, of } from 'rxjs';

/** Una entrada de menú con lo que la pantalla necesita saber de ella. */
interface MenuRow extends MenuAdminItem {
  /** `false` si el enlace no corresponde a ninguna ruta que exista hoy. */
  alive: boolean;
}

/**
 * El menú y quién lo ve, editable.
 *
 * El mapa vive en `msvc-users` —tablas `menus_groups` y `menus_roles`, las
 * mismas que ya servían el menú por rol— así que lo que se guarda aquí lo ve
 * todo el equipo, no sólo el navegador donde se tocó.
 *
 * El catálogo de rutas, en cambio, sigue en código: una entrada sólo puede
 * apuntar a una ruta que exista de verdad. Por eso las entradas viejas —las del
 * front anterior, que apuntan a módulos fuera de circulación— salen marcadas
 * como enlace roto en vez de desaparecer sin explicación.
 */
@Component({
  selector: 'app-access-matrix',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './access-matrix.html',
  imports: [
    FormsModule,
    MatButtonModule,
    MatIconModule,
    MatTooltipModule,
    PageHeader,
    SectionCard,
    StatCard,
  ],
})
export default class AccessMatrix {
  private menus = inject(MenuAdminService);
  private access = inject(AccessService);
  private destroyRef = inject(DestroyRef);

  /** Las rutas que existen hoy: lo único a lo que una entrada puede apuntar. */
  protected readonly catalog = VIEWS;
  protected readonly myRoles = this.access.roles;

  protected rows = signal<MenuRow[]>([]);
  protected loading = signal(true);
  /** Mensaje cuando la API no responde: sin inventar que todo está bien. */
  protected error = signal<string | null>(null);
  protected saving = signal(false);

  /** Roles conocidos: los del realm más los que ya usa algún menú. */
  protected knownRoles = signal<string[]>([...ROLES]);

  protected selected = signal<MenuRow | null>(null);
  /** Copia editable de la entrada seleccionada. */
  protected draft = signal<MenuAdminItem | null>(null);

  protected alive = computed(() => this.rows().filter((r) => r.alive).length);
  protected broken = computed(() => this.rows().filter((r) => !r.alive).length);
  protected orphan = computed(
    () => this.rows().filter((r) => !r.roles?.length).length
  );

  /** El rol cuyo menú se está previsualizando. */
  protected previewRole = signal<string>('ADMIN');

  protected preview = computed(() =>
    this.rows()
      .filter((r) => r.alive && r.roles?.includes(this.previewRole()))
      .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
  );

  constructor() {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.error.set(null);

    this.menus
      .list()
      .pipe(
        catchError(() => {
          this.error.set(
            'No se pudo leer el menú de msvc-users. Necesita el servicio arriba y un token con rol ADMIN.'
          );
          return of([] as MenuAdminItem[]);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((items) => {
        this.rows.set((items ?? []).map((item) => this.decorate(item)));
        this.loading.set(false);
      });

    this.menus
      .roles()
      .pipe(
        catchError(() => of([] as string[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((roles) => {
        // Los del realm que conozco, más los que ya se usan en la base.
        const union = new Set([...ROLES, ...(roles ?? [])]);
        this.knownRoles.set([...union].sort());
      });
  }

  /** Marca si el enlace de la entrada apunta a una ruta que exista hoy. */
  private decorate(item: MenuAdminItem): MenuRow {
    const link = (item.link ?? '').trim();
    return {
      ...item,
      roles: item.roles ?? [],
      alive: !!link && this.catalog.some((view) => view.route === link),
    };
  }

  select(row: MenuRow): void {
    this.selected.set(row);
    this.draft.set({ ...row, roles: [...(row.roles ?? [])] });
  }

  /** Entrada nueva, ya apuntando a una ruta del catálogo. */
  nueva(): void {
    const view = this.catalog[0];
    this.selected.set(null);
    this.draft.set({
      title: view.label,
      icon: view.icon,
      link: view.route,
      type: 'basic',
      order: (this.rows().length + 1) * 10,
      roles: ['ADMIN'],
    });
  }

  /** Al elegir una ruta del catálogo, arrastra su etiqueta e icono. */
  pickRoute(route: string): void {
    const view = this.catalog.find((v) => v.route === route);
    this.draft.update((d) =>
      d
        ? {
            ...d,
            link: route,
            title: d.title || (view?.label ?? ''),
            icon: view?.icon ?? d.icon,
          }
        : d
    );
  }

  toggleRole(role: string): void {
    this.draft.update((d) => {
      if (!d) {
        return d;
      }
      const roles = d.roles ?? [];
      return {
        ...d,
        roles: roles.includes(role)
          ? roles.filter((r) => r !== role)
          : [...roles, role],
      };
    });
  }

  hasRole(role: string): boolean {
    return !!this.draft()?.roles?.includes(role);
  }

  guardar(): void {
    const draft = this.draft();
    if (!draft?.title?.trim()) {
      this.error.set('La entrada necesita un título.');
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const request = draft.pk
      ? this.menus.update(draft.pk, draft)
      : this.menus.create(draft);

    request
      .pipe(
        catchError(() => {
          this.error.set(
            'No se pudo guardar. ¿El token sigue siendo de ADMIN?'
          );
          this.saving.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((saved) => {
        this.saving.set(false);
        if (!saved) {
          return;
        }
        this.draft.set(null);
        this.selected.set(null);
        this.load();
      });
  }

  borrar(row: MenuRow): void {
    if (!row.pk) {
      return;
    }
    this.saving.set(true);
    this.menus
      .remove(row.pk)
      .pipe(
        catchError(() => {
          this.error.set('No se pudo borrar la entrada.');
          this.saving.set(false);
          return of(null);
        }),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe(() => {
        this.saving.set(false);
        this.selected.set(null);
        this.draft.set(null);
        this.load();
      });
  }

  cancelar(): void {
    this.draft.set(null);
    this.selected.set(null);
  }
}
