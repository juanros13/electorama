import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/move-advance'),
  },
  // TODO(Oleada C): vistas de mapa (avance por municipio/distrito con choropleth).
];

export default routes;
