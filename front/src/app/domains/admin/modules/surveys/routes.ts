import { Routes } from '@angular/router';

const ph = () => import('@/app/shared/placeholder/placeholder');

const routes: Routes = [
  // Lista de censos por capturar (landing).
  {
    path: '',
    loadComponent: () => import('./features/surveys-list'),
  },
  // Lista de censos completados.
  {
    path: 'complete',
    loadComponent: () => import('./features/surveys-list-complete'),
  },
  // Captura/edición completa de un censo: contenedor (header + imágenes) + form de 6 pasos.
  {
    path: 'fulldata/:id',
    loadComponent: () => import('./features/full-data-survey'),
  },
  // Solo el form de captura (sin el header del contenedor).
  {
    path: 'update/:id',
    loadComponent: () => import('./features/surveys-form'),
  },
  // Carga única de cédula (PDF/imagen) de un censo.
  {
    path: 'uploadcedula/:id',
    loadComponent: () => import('./features/upload-survey-cedula'),
  },
  // Datos extendidos de miembros (discapacidad / tercera edad) + documentos.
  {
    path: 'extended/:id',
    loadComponent: () => import('./features/extended-data-members'),
  },
  // Asignación de folios y equipos a capturistas (admin).
  {
    path: 'asignsection',
    loadComponent: () => import('./features/assign-section-list'),
  },
  // Sub-módulo de reportes de censo.
  {
    path: 'reports',
    loadChildren: () => import('../surveys-report/routes'),
  },
];

export default routes;
