# Entorno local sin Docker (PG 13 local + Keycloak remoto)

Cómo quedó montado el 30-oct-2026 para probar todo en local contra la base `electorama`. Es la
alternativa al `docker compose` del README cuando Docker no está disponible.

## 1. Base

En el Postgres local (`localhost:5432`, PG 13.1 + PostGIS 3.2), como `postgres`:

```sql
CREATE ROLE electorama LOGIN PASSWORD '...';
ALTER DATABASE electorama OWNER TO electorama;
ALTER SCHEMA public OWNER TO electorama;
\i infra/db/migrations/V0__extensions.sql      -- postgis, unaccent, btree_gist: requieren superusuario
```

Después, como `electorama`, cada migración en su propia transacción (usan `SET LOCAL`):

```bash
for f in infra/db/migrations/V{1,2,3}__*.sql; do psql -d electorama -v ON_ERROR_STOP=1 -1 -f "$f"; done
LEGACY_PGPORT=5432 PGUSER=electorama PGDATABASE=electorama ./infra/db/reference/load-reference.sh
for f in infra/db/migrations/V{4,5,6,7,8}__*.sql; do psql -d electorama -v ON_ERROR_STOP=1 -1 -f "$f"; done
```

V4 y V5 van **después** de `load-reference.sh`, porque le agregan el estado a lo que ya se copió de
Tabasco. No hay `flyway_schema_history`: si después se usa Flyway, hay que hacer `baseline` en V6.

| Migración | Qué hace |
|---|---|
| V4 `territory_states` | `structure.territory` de cualquier estado: código único por `(state_geo_id, code)` |
| V5 `electoral_history_states` | `state_id` en el historial electoral (ver abajo) |
| V6 `campaign_goal` | `structure.campaign.goal_percentage` (61 por default) |
| V7 `compat_campaign` | las vistas compat escriben en la campaña activa de la sección |
| V8 `drop_compat_2024` | quita la capa compat del flujo 2024; sólo queda la lectura y la marca de voto |

## 2. Datos de referencia nacionales

```bash
# lista nominal por sección de los 32 estados (DERFE, corte 31-oct-2025) y meta = LN × %
python infra/db/reference/ln-nacional.py <DatosAbiertos-derfe-pdln_edms_sexo_20251031_Nal_Ext.xlsx> ln_nacional.txt
psql -d electorama -v ON_ERROR_STOP=1 -v cut=2025-10-31 -f infra/db/reference/ln-nacional.sql < ln_nacional.txt

# estados, municipios y secciones vigentes de geography en structure.territory
psql -d electorama -v ON_ERROR_STOP=1 -f infra/db/reference/territory-nacional.sql
```

Resultado: 32 estados, 2,477 municipios y 73,272 secciones en `structure.territory`; LN en 72,7xx
secciones. Tabasco queda igual que antes (LN 1,818,830, techo 1,109,481).

## 3. Historial electoral federal de los 32 estados

Las fuentes del INE ya están en `C:\Users\juanr\geo-2026\electoral-history\fuentes\ine\`.

```bash
cd C:\Users\juanr\geo-2026\electoral-history\fuentes
N=../nacional
for e in "2024 PRESIDENT" "2024 SENATOR" "2024 FEDERAL_DEPUTY" "2021 FEDERAL_DEPUTY"; do
  python <repo>/back/electorama-msvc-electoral-history/tools/normalize/normalize.py $e $N --state all
done
python <repo>/back/electorama-msvc-electoral-history/tools/normalize/ine_catalog.py national $N/catalog_federal.sql \
  2024:PRESIDENT 2024:SENATOR 2024:FEDERAL_DEPUTY 2021:FEDERAL_DEPUTY

psql -d electorama -f <repo>/infra/db/reference/electoral-history/prepare-states.sql
cd $N && psql -d electorama -f catalog_federal.sql
for e in "2024 PRESIDENT" "2024 SENATOR" "2024 FEDERAL_DEPUTY" "2021 FEDERAL_DEPUTY"; do
  set -- $e
  psql -d electorama -v ON_ERROR_STOP=1 -v year=$1 -v office=$2 -v state= \
       -f <repo>/infra/db/reference/electoral-history/load-results.sql
done
psql -d electorama -c "REFRESH MATERIALIZED VIEW electoral_history.mv_section_turnout" \
                   -c "REFRESH MATERIALIZED VIEW electoral_history.mv_section_group"
```

| Elección | Casillas | Votos |
|---|---|---|
| Presidencia 2024 | 170,766 | 60,115,184 (igual al cómputo del INE) |
| Senado 2024 | 170,616 | 59,391,291 |
| Diputaciones federales 2024 | 170,465 | 59,034,323 |
| Diputaciones federales 2021 | 162,793 | 48,874,040 |

En 2021, 2,040 casillas están en secciones que el INE reseccionó después y no aparecen en ningún
catálogo: cuentan en el estado y su distrito federal, pero quedan en el municipio 0.

El refresco de `mv_section_group` con los 32 estados tarda (decenas de minutos en PG 13).

### Locales de la Ciudad de México (IECM)

Bases "por casilla electoral" del Sistema de Consulta de la Estadística de Resultados del IECM,
en `fuentes/iecm/<año>/` (sha256 en `fuentes/manifest.txt`):

| Elección | Archivo | Origen |
|---|---|---|
| Jefatura de Gobierno 2024 (`GOVERNOR`) | `bd2024jgcas.xlsx` | `estadisticaresultadospelo2024.iecm.mx/archivos/downloadjg.php` |
| Diputaciones MR 2024 | `bd2024dmrcas.xlsx` | `…/downloaddmr.php` |
| Alcaldías 2024 (`MAYOR`) | `bd2024alccas.xlsx` | `…/downloadalc.php` |
| Diputaciones MR 2021 | `bd2021dmrcas.xls` | `www.iecm.mx/www/estadisticaresultadospelo2021/archivos/downloaddmr.php` |
| Alcaldías 2021 | `bd2021alccas.xls` | `…/downloadalc.php` |

Las de 2021 están detrás del filtro anti-bots de `www.iecm.mx`: se bajan con el navegador. Los
`.xls` de 2021 se leen con `xlrd` (`pip install xlrd==2.0.1`).

```bash
cd C:\Users\juanr\geo-2026\electoral-history\fuentes
T=<repo>/back/electorama-msvc-electoral-history/tools/normalize
for e in "2024 GOVERNOR" "2024 LOCAL_DEPUTY" "2024 MAYOR" "2021 LOCAL_DEPUTY" "2021 MAYOR"; do
  python $T/normalize.py $e ../cdmx --source iecm
done
python $T/iecm_catalog.py ../cdmx ../cdmx/catalog_iecm.sql   # partido ELIGE, fuentes, elecciones, boleta y candidaturas
cd ../cdmx && psql -d electorama -f catalog_iecm.sql
for e in "2024 GOVERNOR" "2024 LOCAL_DEPUTY" "2024 MAYOR" "2021 LOCAL_DEPUTY" "2021 MAYOR"; do
  set -- $e
  psql -d electorama -v ON_ERROR_STOP=1 -v year=$1 -v office=$2 -v state=9 \
       -f <repo>/infra/db/reference/electoral-history/load-results.sql
done
# y el REFRESH de las dos vistas
```

| Elección | Casillas | Votos |
|---|---|---|
| Jefatura de Gobierno 2024 | 13,466 | 5,551,038 (Brugada 2,882,138 · Taboada 2,155,967 · MC 408,668) |
| Diputaciones MR 2024 | 13,465 | 5,543,221 |
| Alcaldías 2024 | 13,474 | 5,504,567 |
| Diputaciones MR 2021 | 13,092 | 4,029,984 |
| Alcaldías 2021 | 13,160 | 4,051,513 |

Veracruz (OPLE) sigue sin locales: el OPLE no publica una base por casilla descargable (el
SICDM 2021 no responde y el portal 2024 no tiene bases).

## 4. Keycloak remoto

Realm `electorama` en `$KEYCLOAK_URL` (el servidor remoto). En la consola:

1. **Realm settings → Action → Partial import** con `infra/keycloak/electorama-realm.json` (clientes
   y roles; *Skip* si ya existen). Crea `electorama-front` (público) y `electorama-back`
   (confidencial).
2. **Clients → electorama-back → Credentials**: el secreto va a `KEYCLOAK_CLIENT_SECRET` en `.env`.
3. Un usuario con rol `ADMIN` en el realm, para entrar al front.
4. msvc-users administra el realm (crea usuarios y los roles de los niveles) con una cuenta de
   servicio, sin tocar `master`:
   - **Clients → Create client** `electorama-users`: *Client authentication* ON, sólo
     *Service accounts roles* (sin standard flow ni direct access).
   - **Service accounts roles → Assign role → Filter by clients**: `realm-management` →
     `manage-users`, `view-users`, `manage-realm`, `view-realm`.
   - **Credentials**: el secreto va a `KEYCLOAK_ADMIN_CLIENT_SECRET` (con
     `KEYCLOAK_ADMIN_CLIENT_ID=electorama-users`).

   Si `KEYCLOAK_ADMIN_CLIENT_SECRET` está vacío, cae al modo anterior: usuario admin de `master`
   (`KEYCLOAK_ADMIN_USER` / `KEYCLOAK_ADMIN_PASSWORD`) con `admin-cli`.

`.env` apunta a ese Keycloak y `front/public/config.json` también (`url_keycloak`,
`keycloak_realm: "electorama"`): ese cambio del front es **local, no se commitea**.

## 5. Back y front

Java 17 (`C:\Program Files\Java\jdk-17.0.3`; el `java` del PATH es 11).

```bash
set -a; . ./.env; set +a
export SPRING_PROFILES_ACTIVE=local SPRING_CLOUD_KUBERNETES_ENABLED=false
export JAVA_HOME="/c/Program Files/Java/jdk-17.0.3"; export PATH="$JAVA_HOME/bin:$PATH"
cd back && mvn -q -Dmaven.test.skip=true install
java -jar electorama-msvc-gateway/target/*.jar &           # :8090
java -jar electorama-msvc-users/target/*.jar &             # :8001
DDL_AUTO=none java -jar electorama-msvc-geography/target/*.jar &  # :9003 (sus entidades heredadas no pasan validate)
java -jar electorama-msvc-structure/target/*.jar &         # :9014
java -jar electorama-msvc-electoral-history/target/*.jar & # :9015
cd ../front && npm run start:local                          # http://localhost:3873
```
