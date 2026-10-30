# Playbook: portar un módulo (Oleada A)

> Patrón establecido portando `citizen-issues` (lista). Seguir estos pasos por cada módulo de negocio. Objetivo: consistencia y velocidad.

## Estructura destino

```
src/app/domains/admin/modules/<modulo>/
  routes.ts                 # export default Routes; loadComponent por feature
  data/
    <modulo>.types.ts       # interfaces (sin lógica)
    <modulo>.service.ts     # HttpClient + AppConfigService, inject()
  features/
    <modulo>-list.ts/.html  # contenedor standalone (default export)
    <modulo>-form.ts/.html
  ui/                       # (opcional) componentes presentacionales
```

Registrar en `domains/admin/routes.ts` antes del catch-all:
`{ path: '<modulo>', loadChildren: () => import('./modules/<modulo>/routes') }`.

## Convenciones (no negociables)

1. **Standalone + `export default`** en cada feature; `imports: [...]` explícitos (Material por módulo, no un SharedModule global).
2. **`inject()`** siempre; nada de constructor DI. Clases de servicio conservan sufijo `Service`.
3. **`ChangeDetectionStrategy.OnPush`** en todos los componentes (obligatorio en zoneless).
4. **Signals** para estado (`signal`/`computed`); `takeUntilDestroyed(inject(DestroyRef))` para suscripciones; nada de `takeUntil+Subject` manual.
5. **Control flow** `@if/@for/@switch` (sin `*ngIf/*ngFor`).
6. **Alias `@/`** para imports de `core`/`shared`; relativos dentro del módulo.
7. **Config de runtime**: las URLs salen de `AppConfigService.config.url_*` leídas **dentro de los métodos** del service. Nunca `environment.*`.
8. **i18n**: pendiente de estandarizar con transloco; por ahora español directo (TODO transversal).

## Servicio (patrón)

```ts
@Injectable({ providedIn: 'root' })
export class FooService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private get base() { return this.appConfig.config.url_foo; }

  getAll(...): Observable<...> { return this.http.get(`${this.base}/...`); }
}
```
- Quitar el `map(r => r)`/`catchError(throwError)` redundante del viejo; el manejo de error vive en el componente o el interceptor.
- Tipar retornos; corregir bugs detectados al portar (p.ej. `Provider` se importaba de `@angular/core`; `Content-Type: multipart/form-data` manual rompe FormData → quitarlo).

## Lista con paginación server-side (patrón)

- `viewChild.required(MatPaginator)` + `viewChild.required(MatSort)`.
- En `ngAfterViewInit`: `merge(sort.sortChange, paginator.page, search$).pipe(startWith(null), switchMap(() => service.getX(...).pipe(catchError(() => of(null)))), takeUntilDestroyed(destroyRef)).subscribe(res => { data.set(res?.content ?? []); resultsLength.set(res?.totalElements ?? 0); })`.
- `search()` → `paginator().pageIndex = 0; search$.next()`.
- Roles: `UserService.hasRole(RolType.X)` ahora es **síncrono** → usar en `computed()` (ej. `isAdmin`).
- Catálogos "TODOS": sembrar el signal con el item `{id:0,...}` y concatenar la respuesta.

## Reemplazos de dependencias

| Viejo | Nuevo |
|------|------|
| `@fuse/animations` | quitar (CSS/Tailwind) |
| `@fuse/confirmation` / `SwalTypes` | `@/app/shared/ui/swal` (`confirmDialog/showLoading/closeLoading/showSuccess/showError`) |
| `@ngrx/store` (download) | signals locales (`loading`, etc.) — store global diferido |
| `moment` | `date-fns` o `DatePipe` |
| `environment.*` | `AppConfigService` |
| `UserService.hasRole()` Observable | `hasRole()` síncrono (signal) |
| rutas `'/x'` | `'/admin/x'` |

## Diferir / no portar

- Módulos demo `apps/`, `example/`, `mock-api`.
- Libs pesadas hasta su oleada: `ng2-pdf-viewer`→`ngx-extended-pdf-viewer`, `apexcharts` (dashboards), `leaflet`→`@bluehalo/ngx-leaflet`, `fullcalendar` 6 (ver `02-spikes`).
- Lo no portado de un módulo (form gigante, dashboard) → ruta a `@/app/shared/placeholder/placeholder`.

## Checklist por módulo

- [ ] `data/` (types + service con AppConfig/inject)
- [ ] `features/` (standalone, OnPush, signals, control flow)
- [ ] `routes.ts` + registro en admin
- [ ] reemplazos (swal, fechas, config, roles)
- [ ] `npx tsc --noEmit -p tsconfig.app.json` y `npm run build` verdes
- [ ] navegar la ruta y verificar contra el viejo en paralelo

## Estado citizen-issues (referencia)

Portado: service + types + **lista** (con paginación, filtros, stats, soft/hard delete).
Pendiente (placeholder): form de alta/edición (stepper, pdf, image-compress), solución (FollowerService), dashboard (apexcharts). i18n con transloco.
