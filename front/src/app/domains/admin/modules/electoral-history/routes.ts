import { canSee } from '@/app/core/access/access.guard';
import { Routes } from '@angular/router';

/** Histórico electoral: resultados oficiales de 2018, 2021 y 2024 en Tabasco. */
const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
  {
    path: 'dashboard',
    canMatch: [canSee('electoralHistory.dashboard')],
    loadComponent: () => import('./features/electoral-history-dashboard'),
  },
  {
    // la sección de quien trabaja la estructura (movilizador, promotor, referente)
    path: 'my-section',
    canMatch: [canSee('electoralHistory.mySection')],
    loadComponent: () => import('./features/section-history'),
  },
];

export default routes;
