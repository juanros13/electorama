import { authGuard, noAuthGuard } from '@/app/core/auth/auth.guard';
import { Route } from '@angular/router';

export const routes: Route[] = [
  // Website routes
  {
    path: 'home',
    loadChildren: () => import('./domains/website/routes'),
  },

  // Auth (solo accesible sin sesión)
  {
    path: 'auth',
    canMatch: [noAuthGuard],
    loadChildren: () => import('./domains/auth/routes'),
  },

  // Admin (requiere sesión)
  {
    path: '',
    pathMatch: 'full',
    redirectTo: 'admin',
  },
  {
    path: 'admin',
    canMatch: [authGuard],
    loadChildren: () => import('./domains/admin/routes'),
  },

  // Coming soon
  {
    path: 'coming-soon',
    loadChildren: () => import('./domains/coming-soon/routes'),
  },
];
