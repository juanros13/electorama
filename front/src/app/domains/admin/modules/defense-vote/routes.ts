import { Routes } from '@angular/router';

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'polling-booth-report' },
  {
    path: 'polling-booth-report',
    loadComponent: () => import('./features/polling-booth-report'),
  },
];

export default routes;
