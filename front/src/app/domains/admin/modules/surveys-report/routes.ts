import { Routes } from '@angular/router';

const ph = () => import('@/app/shared/placeholder/placeholder');

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'municipalitiesprogress' },
  // Avance por municipio (landing de reportes).
  {
    path: 'municipalitiesprogress',
    loadComponent: () => import('./features/reports-progress'),
  },
  // Detalle de avance de un municipio (stats + mapa de casas censadas).
  {
    path: 'municipalitydetails/:id/:name',
    loadComponent: () => import('./features/state-report'),
  },
  // Reporte estatal por municipio (tabla + CSV).
  {
    path: 'municipalitylist',
    loadComponent: () => import('./features/report-general-list'),
  },
  // TODO Oleada B: progreso general, resumen, edición.
  // Progreso general (paginado server-side por servidor).
  {
    path: 'generalprogress',
    loadComponent: () => import('./features/report-general-progress'),
  },
  // Total por municipio (avance censo + cédulas).
  {
    path: 'municipalitytotal',
    loadComponent: () => import('./features/report-server-municipal'),
  },
  // Censos capturados por un servidor estatal.
  {
    path: 'serverreport/:id',
    loadComponent: () => import('./features/report-server-state'),
  },
  // Resumen completo (solo lectura) de un censo.
  {
    path: 'surveysummary/:id',
    loadComponent: () => import('./features/survey-summary'),
  },
  // Edición completa de un censo: generales + familiares + anexos.
  {
    path: 'editsurvey/:id',
    loadComponent: () => import('./features/edit-complete-survey/edit-complete-survey'),
  },
];

export default routes;
