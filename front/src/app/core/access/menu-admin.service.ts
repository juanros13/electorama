import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';

/** Tipo de entrada, tal como lo guarda `menus_groups.type`. */
export type MenuType = 'basic' | 'group' | 'collapsable' | 'aside' | 'divider';

/** Una entrada del menú con los roles que la ven. */
export interface MenuAdminItem {
  pk?: number;
  id?: string;
  title: string;
  subtitle?: string;
  icon?: string;
  link?: string;
  type?: MenuType;
  order?: number;
  roles: string[];
}

/**
 * El menú y sus roles, contra msvc-users.
 *
 * Es la fuente de verdad: lo que se guarda aquí es lo que ve el equipo, no lo
 * que alguien dejó tocado en su navegador. El mapa de `access.ts` queda como
 * catálogo de rutas que existen de verdad y como respaldo si el servicio no
 * responde.
 */
@Injectable({ providedIn: 'root' })
export class MenuAdminService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return `${this.appConfig.config.url_user}/menu/v1.0.0/admin`;
  }

  list(): Observable<MenuAdminItem[]> {
    return this.http.get<MenuAdminItem[]>(`${this.base}/menus`);
  }

  /** Los roles que ya se usan en algún menú, para no teclearlos a mano. */
  roles(): Observable<string[]> {
    return this.http.get<string[]>(`${this.base}/roles`);
  }

  create(menu: MenuAdminItem): Observable<MenuAdminItem> {
    return this.http.post<MenuAdminItem>(`${this.base}/menus`, menu);
  }

  update(pk: number, menu: MenuAdminItem): Observable<MenuAdminItem> {
    return this.http.put<MenuAdminItem>(`${this.base}/menus/${pk}`, menu);
  }

  /** Reemplaza los roles de una entrada. Lista vacía: no la ve nadie. */
  setRoles(pk: number, roles: string[]): Observable<MenuAdminItem> {
    return this.http.put<MenuAdminItem>(
      `${this.base}/menus/${pk}/roles`,
      roles
    );
  }

  remove(pk: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/menus/${pk}`);
  }
}
