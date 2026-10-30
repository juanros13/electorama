import { Routes } from '@angular/router';

const placeholder = () => import('@/app/shared/placeholder/placeholder');

const routes: Routes = [
  // TODO(Oleada A): listado de simpatizantes día-D (toggle de movilización).
  { path: '', loadComponent: placeholder },
  // Resumen de movilización (avance por municipio/distrito).
  { path: 'reports', loadComponent: () => import('./features/mobilization-report') },
  // TODO: detalle por municipio y reportes por referente.
  { path: 'reports/municipality', loadComponent: placeholder },
  { path: 'reports/referent', loadComponent: placeholder },
  { path: 'reports/referentAlt', loadComponent: placeholder },
];

export default routes;
