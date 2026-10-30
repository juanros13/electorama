import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: '',
    loadComponent: () => import('./features/citizen-issues-list'),
  },
  // Alta de incidencia (stepper + FormArray + campos condicionales + PDF/imagen).
  {
    path: 'add',
    loadComponent: () => import('./features/citizen-issues-form'),
  },
  // Edición de un detalle de incidencia (mismo form, modo edición por :id).
  {
    path: 'update/:id',
    loadComponent: () => import('./features/citizen-issues-form'),
  },
  // Registro / consulta de la solución de una incidencia (seguimiento).
  {
    path: 'solution/:id',
    loadComponent: () => import('./features/citizen-issues-solution-form'),
  },
  {
    path: 'dashboard',
    loadComponent: () => import('@/app/shared/placeholder/placeholder'),
  },
];

export default routes;
