import { canSee } from '@/app/core/access/access.guard';
import { Routes } from '@angular/router';

/**
 * Estructura.
 *
 * El organigrama del modelo nuevo, los convencidos (listado y captura) y el
 * catálogo territorial. Cada vista lleva
 * su propio guard porque un rol puede ver una sin la otra.
 *
 * Las vistas viejas —alta y listado de coordinadores contra `msvc-users`—
 * salen de circulación con el resto del front hasta que se rehagan sobre la
 * API de estructura; el código sigue en `features/` y en el historial.
 *
 *   config, coordinators  → features/coordinators-list
 *   addcoordinator        → features/coordinator-form
 *   localities, sections  → territory (catálogo territorial)
 *   canvasandwallsgoals   → placeholder
 */
const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'organization' },
  {
    path: 'dashboard',
    canMatch: [canSee('structure.dashboard')],
    loadComponent: () => import('./features/campaign-dashboard'),
  },
  {
    path: 'organization',
    canMatch: [canSee('structure.organization')],
    loadComponent: () => import('./features/organization-tree'),
  },
  {
    path: 'supporters',
    canMatch: [canSee('structure.supporters')],
    loadComponent: () => import('./features/supporter-list'),
  },
  {
    path: 'capture',
    canMatch: [canSee('structure.capture')],
    loadComponent: () => import('./features/supporter-capture'),
  },
  {
    path: 'supporters-2024',
    canMatch: [canSee('structure.supporters2024')],
    loadComponent: () => import('./features/supporters-analysis'),
  },
  {
    path: 'territory',
    canMatch: [canSee('structure.territory')],
    loadComponent: () => import('./features/territory-catalog'),
  },
];

export default routes;
