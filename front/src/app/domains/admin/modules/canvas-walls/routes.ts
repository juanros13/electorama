import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/canvas-walls-list'),
  },
  // TODO(Oleada C): alta de lona/barda (mapa + evidencia fotográfica) y reportes.
  {
    path: 'add',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
];

export default routes;
