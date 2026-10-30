# Spikes de dependencias de riesgo — resultados (Fase 0)

> Objetivo: confirmar que existe ruta a Angular 21 para las deps de riesgo antes de comprometer la Oleada C (maps, canvas-walls, visits, calendar). Verificado vía `peerDependencies.@angular/core` en npm.

## Veredicto: las 4 tienen ruta A21 confirmada ✅

| Dep vieja | Reemplazo / versión A21 | peer `@angular/core` | Riesgo restante |
|---|---|---|---|
| `@asymmetrik/ngx-leaflet` 17 (tope v18) | **`@bluehalo/ngx-leaflet` 21.2.1** (sucesor oficial) | `21` | Bajo — cambia el nombre del paquete; API similar. `leaflet` 1.9.4 y `leaflet.markercluster` (JS puro) se mantienen. |
| `@fullcalendar/angular` 5.11 | **`@fullcalendar/angular` 6.1.20** | `12 - 21` | Medio — el paquete es compatible; el trabajo es la **migración de API 5→6** y que el CSS ya no se importa desde JS (elimina `remove-css-imports.js`). |
| `ng2-pdf-viewer` 7 | **`ngx-extended-pdf-viewer` 28** | `>=19 <23` | Bajo/Medio — cambia API y assets de pdf.js; más features. |
| `ngx-mask` 13 | **`ngx-mask` 21.0.1** | `^21` | Bajo — upgrade directo, usa providers standalone. |
| `ng-apexcharts` (theme trae 2.3.0) | `2.4.0` | `>=20` | Bajo — ya en el theme. |

## Conclusión

El riesgo de **disponibilidad/compatibilidad** (el peor, porque no tiene workaround) queda **resuelto**: ninguna de las deps de riesgo bloquea la migración. Lo que queda es trabajo de **migración de API**, que se hace durante la **Oleada C** al portar maps/canvas-walls/calendar/visits, no antes:

- **Leaflet:** instalar `@bluehalo/ngx-leaflet@21` + `leaflet@1.9.4` + `@types/leaflet` + `leaflet.markercluster`; portar los componentes de `maps/` y el mapa de `canvas-walls`. Validar render en zoneless (OnPush + signals).
- **FullCalendar:** subir a 6, ajustar imports/inputs del componente `calendar` (1358 líneas — partir al portar), quitar `remove-css-imports.js`, importar el CSS por `styles`.
- **PDF:** cambiar `surveys/` a `ngx-extended-pdf-viewer`, configurar assets de pdf.js en `angular.json`.

**Spike runtime completo (componente aislado) recomendado al inicio de la Oleada C**, no ahora: en esta fase basta con la compatibilidad confirmada arriba. Hacer componentes throwaway ahora aporta menos que confirmar versiones.
