import { Routes } from '@angular/router';

const placeholder = () => import('@/app/shared/placeholder/placeholder');

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'sheetreview' },
  // Resultado de actas (Oleada B — portado).
  { path: 'sheetreview', loadComponent: () => import('./features/sheet-review') },
  // PREP / participación (Oleada B — portado).
  { path: 'participation', loadComponent: () => import('./features/advance-participation') },
  // Validación de actas — lista + diálogo (Oleada B — portado).
  { path: 'sheetvalidation', loadComponent: () => import('./features/sheet-list') },
  // Flujo de votación (Oleada B — portado).
  { path: 'votingflow', loadComponent: () => import('./features/voting-flow-list') },
  // Apertura de casillas (Oleada B — portado).
  { path: 'pollingbooths', loadComponent: () => import('./features/pollingboot-list') },
  // Tablero de votación / marcaje (Oleada B — portado; sin la card de mapa leaflet).
  { path: 'votingdashboard', loadComponent: () => import('./features/voting-dashboard') },
  { path: 'votingdashboard', loadComponent: placeholder },
  { path: 'mobilizationalt', loadComponent: placeholder },
];

export default routes;
