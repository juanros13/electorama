import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/follower-list'),
  },
  // Alta de simpatizante (form v1 — sin SIRENA/mapa lat-long/encuesta detallada).
  {
    path: 'add',
    loadComponent: () => import('./features/follower-form'),
  },
  // TODO: edición, conversión a promotor, listado SIRENA.
  {
    path: 'update/:id',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
  {
    path: 'syren/list',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
];

export default routes;
