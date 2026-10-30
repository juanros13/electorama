# Mapeo `@fuse` (proyecto viejo) → core del theme nuevo

> Fase 0. El theme 21 **no incluye `@fuse` como librería**: es código propio en `src/app/core` + componentes standalone en `domains/admin/layout`. Hay que deconstruir cada símbolo `@fuse` del viejo y reconstruir con: core del theme, Angular Material, Tailwind/CSS o stubs propios.

## Salvedad importante sobre los conteos

Los números de "archivos afectados" del inventario incluyen los módulos **demo `apps/`** (academy, chat, contacts, ecommerce, mailbox, scrumboard, notes, file-manager, help-center…) y `example/`, que **se descartan** en la migración. La carga real sobre código de negocio es bastante menor. Antes de portar cada símbolo, filtrar usos dentro de `modules/admin/apps`, `modules/admin/example` y `mock-api` (todos descartables).

## Equivalentes que SÍ existen en el theme

| `@fuse` viejo | Equivalente en theme | API / nota |
|---|---|---|
| `FuseMediaWatcherService` (`onMediaChange$`, `onMediaQueryChange$`) | `core/media` → `Media.match(query)` | Devuelve **Signal\<boolean\>**, no Observable. `this._media.onMediaChange$.subscribe()` → `inject(Media).match('(max-width: 1023px)')` + `computed()`. Ruta: `core/media/media.ts`. |
| `FuseConfigService` (scheme/tema) | `core/theming` → `Theming` (`scheme`, `colors`, `isDark` signals) | **Parcial**: cubre tema/scheme, no es drop-in de toda la config. Si se necesita config global app, crear servicio propio. Ruta: `core/theming/theming.ts`. |
| `FuseVerticalNavigationComponent` / `FuseNavigationService` | `domains/admin/layout` (`ui/navigation.ts`, `ui/sidebar.ts`, `data/navigation.ts`) | El theme arma el menú en `layout/data/navigation.ts` (datos) + componentes standalone. La navegación **dinámica por rol** del viejo (`core/navigation`) se reimplementa aquí. |
| `FuseDrawerComponent` | `MatSidenav`/`MatSidenavContainer` (ya usado por el layout del theme) | Reemplazo directo por Material. |
| `IconsModule` / `@fuse` icons | `core/icons` (Lucide + MatIconRegistry) | Ruta: `core/icons/provider.ts`. |
| Splash screen / local storage | `core/local-storage`; splash → CSS en `index.html` | — |

## Sin equivalente — estrategia por símbolo

| `@fuse` viejo | Estrategia | Reemplazo | Esfuerzo |
|---|---|---|---|
| `FuseConfirmationService` (negocio: 4, vía SweetAlert2 en muchos sitios) | (a) Port propio | Servicio con `MatDialog` + dialog de confirmación, o estandarizar en SweetAlert2 (ya dependencia del viejo) | Medio |
| `FuseAlertComponent`/`Service` | (b) Reemplazar | `MatSnackBar` o componente toast Tailwind propio | Medio |
| `fuseAnimations` (mayoría en `apps/` descartable) | (c) Eliminar / reemplazar | Transiciones Tailwind/CSS; o `animations:[]` puntual en el componente | Bajo por archivo |
| `FuseNavigationItem` (interface, 19) | (a) Port | Copiar la interface a `core/navigation` y adaptarla al render del theme | Medio |
| `FuseFindByKeyPipe` (4) | (a) Port trivial | Pipe standalone `find(arr, key, val)` | Bajo |
| `FuseValidators.mustMatch` (1) | (a) Port trivial | Validador propio en `utils/validators` | Bajo |
| `FuseUtilsService` (`randomId`, match options) | (b) Reemplazar | `crypto.randomUUID()`; opciones de match propias | Bajo |
| `FusePlatformService` | (b) Reemplazar | CDK `Platform` | Bajo |
| `FuseScrollbarDirective` | (b) Reemplazar | Scroll nativo (el theme ya trae su scrollbar) o stub | Bajo |
| `FuseScrollResetDirective` | (a) Port trivial | scroll (0,0) en `Router` `NavigationEnd` (o `withInMemoryScrolling` ya activo) | Bajo |
| `FuseLoadingBar` / `FuseLoadingService` | (a) Port simple | Componente barra standalone + servicio con signal | Bajo |
| `FuseSplashScreenService` | (a) Port trivial | Clases CSS en `index.html` | Bajo |
| `FuseFullscreen` | (b) Reemplazar | API `document.fullscreenElement` | Bajo |
| `FuseHighlight` | (b) Reemplazar | `highlight.js` directo (solo lo usa `apps/` descartable) | Bajo |
| `FuseMasonry` | descartable | Solo en `apps/notes` (descartable) | — |
| `FuseMockApiService`/`Utils`/`Module` | (c) Eliminar | Mock-api se descarta; se usa backend real | — |
| `FuseModule`/`FuseConfigModule`/`FUSE_APP_CONFIG`/`FUSE_VERSION` | (c) Eliminar | Bootstrap del theme (`app.config.ts`) los reemplaza | — |

## Recomendación

Crear una carpeta de compatibilidad mínima (p.ej. `src/app/core/fuse-compat/` o repartir en `core/*` + `shared/`) con SOLO lo que el código de negocio realmente necesita tras descartar `apps/`:
- `confirmation` (MatDialog o SweetAlert2 estandarizado)
- `alert` (MatSnackBar o toast propio)
- `FuseNavigationItem` (tipo) + render del theme
- `find-by-key` pipe, `mustMatch` validator, `loading-bar`, `scroll-reset`

Todo lo demás (animations, mock-api, masonry, highlight, fullscreen, drawer→MatSidenav) se elimina o se reemplaza por Material/Tailwind durante el port de cada módulo. **No reconstruir `@fuse` completo** — solo lo que sobreviva al descarte de `apps/`.

## Orden sugerido (Fase 1)
1. `core/media`, `core/theming`, `core/icons` ya existen → adaptar usos del viejo.
2. Port de los primitivos pequeños (find-by-key, mustMatch, scroll-reset, loading-bar, confirmation, alert).
3. Reimplementar navegación dinámica por rol sobre `layout/data/navigation.ts`.
4. Eliminar animations/mock-api/etc. a medida que se portan los módulos.
