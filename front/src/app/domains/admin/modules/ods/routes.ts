import { Routes } from '@angular/router';

// Drill-down ODS → metas → indicadores → instrumentos
// (Modelo para la transformación de Tabasco).
const routes: Routes = [
  { path: '', loadComponent: () => import('./features/ods-list') },
  { path: 'goals/:idOds', loadComponent: () => import('./features/ods-goals') },
  { path: 'indicators/:idgoal', loadComponent: () => import('./features/ods-indicators') },
  { path: 'instruments/:idIndicators', loadComponent: () => import('./features/ods-instruments') },
];

export default routes;
