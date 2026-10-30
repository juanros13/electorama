import { canSee } from '@/app/core/access/access.guard';
import { Routes } from '@angular/router';
import { AdminLayout } from './layout/layout';

/**
 * Rutas del admin durante la reconstrucción sobre el modelo de estructura.
 *
 * Sólo vive el organigrama. Las demás vistas —las oleadas A, B y C— siguen en
 * `modules/` y en el historial, pero salen de circulación: no se registran, así
 * que no se empaquetan ni se pueden alcanzar por URL. Se van devolviendo, una a
 * una, conforme se rehacen sobre la API nueva.
 *
 * Cada ruta comentada conserva su path original a propósito: es el contrato que
 * el front viejo y los enlaces guardados esperan, y cambiarlo al reactivarla
 * rompería marcadores y enlaces compartidos.
 */
const routes: Routes = [
  {
    path: '',
    component: AdminLayout,
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'structure' },

      // -----------------------------------------------------------------------
      // Estructura: el modelo nuevo
      // -----------------------------------------------------------------------
      // cada vista de estructura trae su guard: organigrama y catálogo se abren por separado
      {
        path: 'structure',
        loadChildren: () => import('./modules/structure/routes'),
      },

      // -----------------------------------------------------------------------
      // Histórico electoral: resultados oficiales 2018, 2021 y 2024
      // -----------------------------------------------------------------------
      {
        // sin guard aquí: cada vista lleva el suyo (el tablero estatal es
        // sólo ADMIN; «mi sección», de la estructura)
        path: 'electoral-history',
        loadChildren: () => import('./modules/electoral-history/routes'),
      },

      // -----------------------------------------------------------------------
      // Accesos: qué ve cada rol
      // -----------------------------------------------------------------------
      {
        path: 'access',
        canMatch: [canSee('access.matrix')],
        loadChildren: () => import('./modules/access/routes'),
      },

      // -----------------------------------------------------------------------
      // El rol no tiene ninguna vista todavía
      // -----------------------------------------------------------------------
      {
        path: 'sin-acceso',
        loadComponent: () => import('./modules/extras/no-access/no-access'),
      },

      // -----------------------------------------------------------------------
      // Fuera de circulación mientras se rehacen.
      //
      //   dashboards      → modules/dashboard
      //   citizen-issues  → modules/citizen-issues
      //   follower        → modules/follower
      //   promoters       → modules/promoters
      //   mobilization    → modules/mobilization
      //   defense-vote    → modules/defense-vote
      //   elections       → modules/elections
      //   maps            → modules/maps
      //   issuestracker   → modules/issue-tracker
      //   canvas-walls    → modules/canvas-walls
      //   safehouse       → modules/safe-house
      //   movereporter    → modules/movreporter
      //   visits          → modules/visits
      //   surveys         → modules/surveys
      //   ods             → modules/ods
      //   settings        → modules/extras/settings
      //   notifications   → modules/extras/notifications
      //   documentation   → modules/documentation
      //   academy, contacts, file-manager, help-center, notes, tasks
      //                   → modules/apps/* (del theme, nunca fueron de campaña)
      // -----------------------------------------------------------------------

      // 404
      {
        path: '404',
        pathMatch: 'full',
        loadComponent: () =>
          import('./modules/extras/error/features/error-404'),
      },

      // Cualquier otra cosa —incluidas las rutas fuera de circulación— cae aquí
      { path: '**', redirectTo: '404' },
    ],
  },
];

export default routes;
