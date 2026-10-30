import { IsActiveMatchOptions } from '@angular/router';

/**
 * Item de navegación del menú lateral. El menú real se arma en
 * {@link NavigationMenuService} (backend `getMenuByRol` con fallback estático).
 */
export type NavigationItem = {
  id: string;
  label: string;
  description?: string;
  route?: string;
  icon?: string;
  badge?: string;
  children?: NavigationItem[];
  disabled?: boolean;
  expanded?: boolean;
  activeOptions?: { exact: boolean } | IsActiveMatchOptions;
};
