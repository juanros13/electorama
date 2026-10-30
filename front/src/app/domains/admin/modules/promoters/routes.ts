import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/promoters-list'),
  },
  // Alta de promotor (form v1 — sin SIRENA/sustitución/coordinador-municipal).
  {
    path: 'add',
    loadComponent: () => import('./features/promoters-form'),
  },
  // TODO: edición, acceso al sistema, SIRENA y simpatizantes-por-promotor.
  {
    path: 'update/:id',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
  {
    path: 'followers/:id',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
  {
    path: 'syren/list',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
];

export default routes;
