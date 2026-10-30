import { Routes } from '@angular/router';

const placeholder = () => import('@/app/shared/placeholder/placeholder');

const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'nacional' },
  // Mapa estatal (Oleada C — base OSM + municipios; secciones/marcadores/sidebar pendientes).
  { path: 'nacional', loadComponent: () => import('./features/maps-nacional') },
  // Mapa de visitas (Oleada C — cluster de marcadores + capa WMS municipios).
  { path: 'visits', loadComponent: () => import('./features/maps-visits') },
  // TODO(Oleada C): mapa por sección.
  { path: 'section/:section', loadComponent: placeholder },
];

export default routes;
