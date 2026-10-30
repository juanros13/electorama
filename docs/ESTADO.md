# Estado del proyecto

> **30-oct-2026 — migración a electorama.** Los tres repos se unieron en este monorepo (`back/`,
> `front/`, `app/`) con una base de datos nueva y renombrado completo. Ver
> [`MIGRACION.md`](MIGRACION.md). Lo de abajo es el estado previo (corte 27-sep-2026), con los
> nombres de carpeta actualizados; los nombres de rama se refieren a los repos viejos.
>
> Corrección al corte anterior: el back tiene **17** microservicios (se sumó `electoral-history`)
> y el front ya trabajaba sobre `main` (con `feat/design-system-y-censos` mergeada).

## Corte previo — 15-jun / 27-sep-2026

Resumen para retomar sin volver a investigar. Actualizado el 22-sep-2026 al commitear el front y el
microservicio de estructura, y el 27-sep-2026 con el catálogo territorial (ver abajo).

## 27-sep-2026 — catálogo territorial: distritos, secciones y meta

Todo commiteado y verificado **sólo en local**.

- **Techo de meta = LN × % de la sección** (61 % por default, editable por sección). La LN es la
  **oficial del INE**: estadística de padrón y lista nominal por sección de la DERFE en datos
  abiertos, corte 31-oct-2025, marco 2026. La carga es el script G5 de geography; la fuente está en
  `C:\Users\juanr\geo-2026\ine_ln_27_20251031.txt`. Tabasco: 1,191 secciones, LN 1,818,830, techo
  1,109,481. El techo anterior (~33 % de una LN vieja) quedó en `geo_secciones_meta_2026_09_prebackup`.
- **"estructura estatal.xlsx"** (el resumen de la campaña) usa el marco 2024 y otro corte de LN
  (-0.4 %). Su "promotor" es el **coordinador de zona** del sistema (1 por cada 200 de meta) y su
  "movilizador" es el **promotor** (1 por cada 20). La pantalla compara asignados contra requeridos,
  sólo en la estructura Principal.
- Front: `/admin/structure/territory`, cargador global en `core/loading`, tema claro por default.
- **Para producción** faltan correr G1–G5 en geography (en orden) y G4 en structure.
- Local: gateway 8090, users 8001, geography 9003, structure 9014, todos con `DB_HOST` (ahora una sola base `electorama`, ver `.env.example`);
  front con `npm run start:local`.

## Los 3 proyectos activos

| Rol | Carpeta | Stack | Rama | Sin commitear |
|---|---|---|---|---|
| back | `back/` | Spring Boot 3.0.5 · Spring Cloud 2022.0.1 · Java 17 · **16** microservicios | `feat/msvc-structure` | 1 archivo |
| front | `front/` | Angular 21.2.6 · Fuse 21 · Tailwind 4 · node ≥ 24 | `feat/design-system-y-censos` | 0 |
| app | `app/` | Flutter 3.45 / Dart 3.12 · bloc 9 · Drift | `develop` | 1 archivo |

Todo lo demás vive en `_archivo/` (ver `_archivo/LEEME.md`).

---

## FRONT — `front/` · migración a medias ⬅ el trabajo principal

No fue una actualización de librerías, fue una **reescritura desde el theme Fuse 21.1.0**, portando
módulo por módulo desde el front viejo (`_archivo/nucleo-dash-front`, Angular 15 / Fuse 17 / NgRx 15 /
node 16).

**Stack actual:** Angular 21.2.6, Material/CDK/aria 21.2.4, Tailwind 4.2.2, TypeScript 5.9.3, RxJS 7.8.2,
`@jsverse/transloco` 8.2.1, apexcharts 5.10.4, `@bluehalo/ngx-leaflet` 21.2.1 + leaflet 1.9.4,
date-fns 4, sweetalert2 11, eslint 10, vitest 4. **SSR desactivado** (SPA client-only).
Convenciones: zoneless, standalone + `export default`, `inject()`, `OnPush`, signals, `@if/@for`,
alias `@/`, config de runtime vía `AppConfigService` (nunca `environment.*`).

**Completado** (56 commits, 14–15 jun):

- **Fase 0** — `docs/migration/`: inventario de secrets/URLs, mapeo `@fuse` → core del theme nuevo,
  spikes de deps de riesgo, playbook de port de módulo.
- **Fase 1 (core)** — `config.json` + `AppConfigService`, auth ROPC con signal forms + guards,
  layout con navegación backend-driven, proxy a gateway local (`npm run start:local`).
- **Oleada A** — follower, promoters, dashboard, structure, mobilization, defense-vote, citizen-issues.
- **Oleada B** — elections (6 vistas: sheet-review, PREP, validación de actas, flujo de votación,
  apertura de casillas, tablero de marcaje), surveys, surveys-report.
- **Oleada C** — setup de leaflet, maps (cluster + WMS), safe-house, canvas-walls (lista), visits.

**Pendiente** — ninguna de estas deps está todavía en `package.json`; el análisis de compatibilidad ya
está hecho en `docs/migration/02-spikes-deps-riesgo.md`:

| Pendiente | Acción |
|---|---|
| FullCalendar 5.11 → **6.1.20** | portar módulo `calendar` (1358 líneas en el front viejo, conviene partirlo), quitar `remove-css-imports.js`, importar el CSS por `styles` |
| `ng2-pdf-viewer` 7 → **`ngx-extended-pdf-viewer` 28** | cambiar el visor en `surveys/`, configurar los assets de pdf.js en `angular.json` |
| `ngx-mask` 13 → **21.0.1** | upgrade directo, usa providers standalone |
| i18n | TODO transversal: hoy hay español hardcodeado en vez de transloco |

**Ya commiteado** en `feat/design-system-y-censos` (4 commits, árbol limpio, `npm run build` pasa):

1. `feat(ui)` — base del design system: las cuatro primitivas de `shared/ui`, la paleta de marca,
   la fuente Bricolage y `animations.css`.
2. `feat(ods)` — módulo nuevo de objetivos, indicadores e instrumentos.
3. `feat(surveys)` — captura y edición completa de censos: cierra los tres placeholders de la
   oleada B (`fulldata/:id`, `update/:id`, `editsurvey/:id`) y valida CURP contra el servicio
   externo, con la URL en `config.json` y no horneada en el bundle.
4. `refactor(admin)` — el pase de design system sobre 74 archivos de módulos y el layout.

Queda una advertencia del build: el bundle inicial se pasa 5 kB del presupuesto de 1 MB.

---

## APP — `app/` · actualización TERMINADA ✅

Commits clave: `4f47384` "Actualiza a Flutter 3.45 y rediseña la UI a Material 3 Expressive" y
`2254e70` "Migra la persistencia local de Isar a Drift (SQLite)".

**Stack actual:** Flutter 3.44+ / Dart 3.12, bloc 9.2.1 + flutter_bloc 9.1.1, **drift 2.34** +
drift_flutter, dio 5.3, camera 0.12, device_info_plus 13, flutter_dotenv 6, animate_do 5, csv 8.

Encima de la actualización ya se construyó: módulo Followers completo (datos en Drift, `user_settings`
con municipio/secciones, escáner real de INE con cámara + OCR ML Kit), selector de 3 módulos en el
inicio, identidad visual por módulo, captura offline en Convencidos / Visitas / Incidencias con sync.

⚠️ **Deuda menor:** `pubspec.yaml` sigue declarando `sdk: >=3.0.6 <4.0.0` mientras el lock ya resuelve
contra Dart ≥ 3.12 / Flutter ≥ 3.44. Conviene subir la restricción para que falle rápido en una máquina
con SDK viejo.

**Sin commitear:** una línea en `lib/main.dart` — `darkTheme: AppTheme.dark` → `AppTheme.light`, o sea
el modo oscuro desactivado a mano. Decidir si se commitea como decisión de producto o se descarta.

---

## BACK — `back/` · las librerías NO se tocaron

Sigue en **Spring Boot 3.0.5 + Spring Cloud 2022.0.1 + Java 17**, spotless 2.27 (google-java-format),
artefacto `electorama-back` 0.1.0-SNAPSHOT (antes `nucleo-dash-back-kuberbetes` 0.0.5). El `pom.xml` no tiene cambios. Es una versión
ya bastante atrasada — candidata a un upgrade propio más adelante, pero no se empezó.

Lo que se trabajó en junio fue **seguridad**, en la rama `improve/users-security`:

- `64dd4ce3` valida `X-Auth-Id` contra la identidad del token (evita suplantación)
- `8f46db97` externaliza la lista blanca de coordinadores (quita UUIDs hardcodeados)
- `5f9d0474` endurece seguridad y optimiza las llamadas a Keycloak

15 microservicios: gateway, users, followers, geography, logfile, elections, calendar, visits,
canvasandwalls, citizen-issues, elections-results, elections-structure, document-management, polls,
survey. Los manifests de kustomize viven en el mismo repo.

**Lo nuevo:** `electorama-msvc-structure`, el microservicio del modelo de estructura, commiteado en
`feat/msvc-structure` junto con los 20 scripts de la migración y el port de seis facades de
`msvc-followers` a su API. El detalle vive en la nota de Obsidian
`nucleo-dash-propuesta-estructura`. Fases 0–3 completas en local; la 4 a medias (faltan los cinco
servicios de escritura de followers y `msvc-users`); la 5 escrita con guardas y sin ejecutar.

**Sin commitear:** `electorama-msvc-visits/src/main/resources/application.yml` apuntado a Postgres
local. **Es un override de desarrollo — no commitear.**

### 🔒 Pendiente de seguridad

Ese mismo `application.yml` tiene **commiteados** el host de la BD productiva, el usuario y la
contraseña en texto plano como valores por defecto de las variables de entorno. Están en el historial de
git, así que quitarlos del archivo no basta:

1. rotar la credencial de Postgres,
2. dejar `${DB_HOST}` / `${DB_USERNAME}` / `${DB_PASSWORD}` **sin default**, inyectados por el secret de
   Kubernetes,
3. revisar el resto de los microservicios por el mismo patrón.

El inventario de `docs/migration/00-inventario-secrets-urls.md` (en el front) ya cataloga este tipo de
casos.

---

## Por dónde retomar

1. Los 3 pendientes de librerías del front (FullCalendar 6, `ngx-extended-pdf-viewer`,
   `ngx-mask` 21) junto con el port del módulo `calendar` — el hueco más grande que queda.
2. El i18n del front: transloco está instalado y el español sigue hardcodeado.
3. Decidir el `main.dart` de la app y subir la restricción de SDK en `pubspec.yaml`.
4. Cerrar la rama `improve/users-security` del back y rotar la credencial expuesta.

### Comandos

```powershell
# front (requiere node >= 24)
cd front
npm start            # ng serve en :3873
npm run start:local  # con proxy al gateway en localhost:8090

# app
cd app
flutter pub get; flutter run

# back
cd back
mvn -q clean install
```
