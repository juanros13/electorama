import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/safe-house-list'),
  },
  // TODO: alta/edición de casa segura (form con mapa para lat/long).
  {
    path: 'add',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
];

export default routes;
