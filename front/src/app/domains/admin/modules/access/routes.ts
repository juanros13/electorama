import { Routes } from '@angular/router';

/** Accesos: el mapa rol → vistas que arma el menú. */
const routes: Routes = [
  {
    path: '',
    pathMatch: 'full',
    loadComponent: () => import('./features/access-matrix'),
  },
];

export default routes;
