# Electorama

Plataforma de gestión electoral: estructura territorial, simpatizantes, encuestas, casillas,
incidencias, lonas y bardas, e historial electoral.

| Carpeta | Qué es | Stack |
|---|---|---|
| `back/` | 17 microservicios + gateway | Spring Boot 3.0.5 · Spring Cloud 2022.0.1 · Java 17 · Maven |
| `front/` | Panel web | Angular 21 · Fuse 21 · Tailwind 4 · node ≥ 24 |
| `app/` | App móvil de captura offline (lonas y bardas, simpatizantes, visitas, incidencias) | Flutter 3.44+ / Dart 3.12 · bloc · Drift |
| `infra/` | Entorno local: Postgres+PostGIS, migraciones Flyway, Keycloak | Docker Compose |
| `docs/` | Estado del proyecto, notas de migración y SQL histórico | |

## Arranque local

```bash
cp .env.example .env          # completar DB_PASSWORD y KEYCLOAK_ADMIN_PASSWORD como mínimo

# 1. Base de datos + migraciones + Keycloak
docker compose --env-file .env -f infra/docker-compose.yml up -d
docker compose --env-file .env -f infra/docker-compose.yml logs flyway   # debe terminar en "Successfully applied"

# 2. (opcional) datos de referencia desde las bases legadas msvc_dash_*
LEGACY_PGPASSWORD=... PGHOST=localhost PGPORT=5433 PGUSER=electorama PGPASSWORD=... PGDATABASE=electorama \
  ./infra/db/reference/load-reference.sh

# 3. Back (Java 17). Las variables salen de .env
set -a; . ./.env; set +a
cd back && mvn -q -Dmaven.test.skip=true install
java -jar electorama-msvc-gateway/target/*.jar &      # :8090
java -jar electorama-msvc-users/target/*.jar &        # :8001
java -jar electorama-msvc-geography/target/*.jar &    # :9003
java -jar electorama-msvc-structure/target/*.jar &    # :9014  (y el resto según se necesite)

# 4. Front
cd front && npm ci && npm run start:local              # http://localhost:3873

# 5. App
cd app && cp .env.example .env && cp .env.example .env.local && flutter pub get && flutter run
```

Keycloak queda en http://localhost:8080 con el realm `electorama-realm` y el usuario
`admin@electorama.local` / `admin` (pide cambio de contraseña al primer login). El secreto del cliente
`electorama-back` se copia de la consola de Keycloak a `KEYCLOAK_CLIENT_SECRET` en `.env`.

## Base de datos

Una sola base `electorama`, **un schema por servicio** (`users`, `geography`, `structure`,
`followers`, `survey`, …). Cada servicio se conecta con `currentSchema=<schema>,public` y Hibernate
valida contra las entidades (`DDL_AUTO=validate`); el schema lo manda Flyway desde
`infra/db/migrations/`. Detalles en [`docs/MIGRACION.md`](docs/MIGRACION.md).
