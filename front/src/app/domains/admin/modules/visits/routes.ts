import { Routes } from '@angular/router';

const routes: Routes = [
  // Lista de visitas (paginado server-side + búsqueda + reporte CSV).
  {
    path: '',
    loadComponent: () => import('./features/visit-list'),
  },
  // Alta de visitas (stepper + FormArray + mapa por persona visitada).
  {
    path: 'add',
    loadComponent: () => import('./features/visit-create'),
  },
];

export default routes;
