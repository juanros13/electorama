# Migración campania → electorama (30-oct-2026)

Electorama arranca desde cero, con historial git nuevo. Toma el árbol de trabajo de los tres repos
activos de `nucleo/campania`, incluidos los cambios sin commitear:

| Antes | Ahora | Rama de origen |
|---|---|---|
| `nucleo-dash-kubernetes` | `back/` | `feat/msvc-structure` (ya contiene `main`, `improve/users-security` y `fix/safehouse-municipality-npe`) |
| `nucleo-campain-front` | `front/` | `main` local. **No incluye** `origin/main` `2140c2d` "fix: package.json" (bump de Angular 21.2.6 → 21.2.25 de otro autor, del 29-oct) |
| `nucleo-dash-app-canvas-and-walls` | `app/` | `develop` |

`_archivo/` no se migró.

## Renombrado

| Antes | Ahora |
|---|---|
| paquetes `com.nucleo.dash.msvc.*` / `com.nucleodash.*` | `com.electorama.msvc.*` / `com.electorama.*` |
| groupId `com.nucleo.dash.kuberbetes`, módulos `nucleo-dash-msvc-*` | `com.electorama`, `electorama-msvc-*` |
| clases `NucleoDashMsvc*Application` | `ElectoramaMsvc*Application` |
| realm `nucleo-dash-realm`, clientes `nucleo-front-client` / `nucleo-dash-back-client` | `electorama-realm`, `electorama-front` (público) / `electorama-back` |
| front `nucleo-campain-front`, "Núcleo Campaña" | `electorama-front`, "Electorama" |
| app `dash_walls_and_canvas`, `com.example.dash_walls_and_canvas`, BD Drift `dash_walls` | `electorama_app`, `com.electorama.app`, `electorama_app` |

No se renombraron "campaña" ni `Campaign`, porque son términos del dominio. Tampoco las URLs de
servicios externos que siguen vivos: GeoServer (`geoserver.nucleo.rocks`), validación de CURP
(`validacion.nucleostaging.net`) y la carpeta de Dropbox `Aplicaciones/nucleopro`.

## Base de datos

**Antes** había 16 bases `msvc_dash_*`, sin migraciones. Las tablas las creaba Hibernate
(`generate-ddl: true`) o se hacían a mano, y las bases se unían entre sí con `postgres_fdw`.

**Ahora** hay una base `electorama` con un schema por servicio, y los schemas se cruzan directo:

- `followers.followers` y `followers.promoters` son vistas de **lectura** sobre `structure_compat` (más la
  marca de voto del día de la elección, que se escribe por `followers_update`). Desde V8 ya no hay altas ni
  bajas por esas vistas, ni `users.users_seccions`, ni el schema `users_compat`.
- `users` y `followers` leen `structure.*`.
- `structure` lee `geography.*`.

Por esas vistas cruzadas las migraciones son **centrales** (`infra/db/migrations`, aplicadas por el
contenedor `flyway`) y no una por servicio, porque habría ciclos entre servicios.

| Migración | Contenido |
|---|---|
| `V0__extensions.sql` | postgis, unaccent, btree_gist (en `public`) |
| `V1__baseline.sql` | Schema completo |
| `V2__seed_catalogs.sql` | Catálogos de configuración: menús/roles, tipos y estados de elecciones/casillas, partidos, campaña, estructura y 1,217 territorios |
| `V3`–`V7` | Campañas multi-estado y niveles (ver `docs/LOCAL.md` y `docs/CAMPANIAS.md`) |
| `V8__drop_compat_2024.sql` | Fase 5: quita la capa compat del flujo 2024 (ver abajo) |

### Cómo se generó V1

`infra/db/tools/build_baseline.py` une los `pg_dump --schema-only` de las bases legadas:

- `public` pasa al schema del servicio. Los schemas secundarios pasan a `<servicio>_<schema>`
  (`structure_compat`, `users_compat`, `followers_compat`).
- Cada foreign table se reemplaza por el objeto real al que apuntaba. Por ejemplo,
  `str.followers` → `structure_compat.followers` y `geo_ref.municipalities` → `geography.geo_municipios`.
- Toda función recibe un `search_path` explícito (su schema, el del servicio y `public`), porque
  un trigger de `structure` se puede disparar desde una sesión de `followers`.
- Se quitan los servidores FDW, las funciones sueltas de `dblink` (sin uso) y el schema `topology`.
- Se dejan fuera 88 tablas que ningún código usa: respaldos `_prebackup` y `_copy*`, staging de
  cargas INE, tablas de etiquetas de mapas y hojas importadas. La lista está en
  `infra/db/tools/legacy-excluded-tables.json` y en la tabla de abajo. Siguen existiendo en las
  bases viejas.

Cinco servicios no tenían base local: calendar, document_management, logfile, polls y visits.
Su schema sale de Hibernate (`ddl-auto=create`). Polls no tiene entidades.

### Verificado (30-oct-2026, Postgres 13 local)

- V0+V1+V2 se aplican en una transacción sin errores.
- Los 16 servicios con datasource arrancan con `ddl-auto=validate` contra la base nueva (llegan a
  "Initialized JPA EntityManagerFactory"), salvo **geography**. Geography se queda en `none`, como
  antes: varias columnas de `geo_*` son `varchar`/`numeric` donde la entidad dice `Integer`, y las
  consultas nativas las tratan como texto. Por ejemplo, `trim(s.distrito_federal_id)`.
- `load-reference.sh` carga las 44 tablas de referencia. Por ejemplo, 74,335 secciones y 688,243
  votos por casilla. Si se re-ejecuta, omite las tablas que ya tienen datos.

### Qué datos NO se migran

En la base nueva no hay datos personales ni operativos: usuarios, promotores, simpatizantes,
personas, membresías, encuestas, incidencias, lonas y bardas, visitas, bitácora. Los datos de
referencia (geografía INE, historial electoral, casillas, distritos) se copian con
`infra/db/reference/load-reference.sh`.

### SQL histórico

Los scripts que antes se corrían a mano están en `docs/legacy-sql/` como referencia: structure V1–V7,
geography G1–G6, electoral-history E1–E5 y menús de users V1–V10. Su resultado final ya está en V1/V2;
**no hay que volver a correrlos**.

### Tablas excluidas de la baseline

| Schema | Tablas |
|---|---|
| `citizen_issues` | `migracion_24oct` |
| `elections` | `fast_survey_copy1`, `polling_booths_` |
| `elections_results` | `election_result_copy1`, `polling_booths_election_associa` |
| `elections_structure` | `actualiza_rc` |
| `followers` | `coordinator_add_followers`, `meta_seccion`, `meta_vf`, `metacomites`, `promoters_legacy`, `survey_section_meta`, `tabasco_rgs`, `update_follower_mark`, `update_name_v2`, `usertest` |
| `geography` | `Tabla_secciones`, `actualizar_dlocal`, `buff_label_tabasco_*`, `geo_*_prebackup` (10), `geo_colonias_cdmx`, `geo_colonias_tab`, `geo_cp_sepomex`, `geo_distritos_*_municipios`, `geo_label_*` (15), `geo_localidades_tab`, `geo_marco_tabasco`, `geo_pol_localidades_tab`, `lineas_label_pruebas`, `relacion_simbologia_secciones` |
| `geography_staging` | `cmp_secc`, `stg_*` (6) |
| `structure` | `migration_duplicate` |
| `survey` | `Hoja2`, `excel_usersurvey`, `section_geo`, `survey_eliminar`, `survey_user_folio_*` (6 copias) |
| `users` | `limits_prod`, `meta_promotores`, `meta_secciones`, `seccion_municipio`, `submenus`, `tabasco_rgs`, `total_coordinadores`, `update_name_promoters`, `user_seccion_meta`, `users_federal_district`, `users_local_district`, `users_references`, `users_seccions_legacy`, `usuarios_*` (4) |

## Secretos: rotar

Estos valores estaban commiteados en los repos viejos y siguen en su historial. Aquí ya no
aparecen, pero **hay que rotarlos**:

- Contraseña de Postgres de producción (`24.199.78.187`, usuario `electorama`) y la local `postgres`.
- Tokens y app key/secret de Dropbox (estaban repetidos en 10 servicios).
- Secreto del cliente Keycloak del gateway y `keycloak_secret` del front (iba en el bundle público).
- Cliente OAuth de Google Calendar (`client_secret_gd.json`) y los `StoredCredential`.
- `CLIENT_SECRET`, `TOKEN_TEST` y `AUTHID_TEST` del `.env` de la app.

## Pendientes conocidos (ya venían del origen)

- `back/electorama-msvc-followers`: `PreregisterDtoTest` no compila porque los tipos no coinciden con
  el DTO. Se compila con `-Dmaven.test.skip=true`.
- `back/electorama-msvc-followers` no declaraba `spring-boot-maven-plugin` y su jar no era
  ejecutable. **Se agregó.**
- Front: 2 de 17 tests fallan en `core/access/access.spec.ts` (`viewsForRoles`).
- App: `test/widget_test.dart` es la plantilla "Counter increments" de Flutter y falla.
