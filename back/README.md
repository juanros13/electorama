# electorama-back

Microservicios Spring Boot 3.0.5 / Spring Cloud 2022.0.1 / Java 17.

| Módulo | Puerto | Schema |
|---|---|---|
| `electorama-msvc-gateway` | 8090 | — |
| `electorama-msvc-users` | 8001 | `users` |
| `electorama-msvc-calendar` | 9001 | `calendar` |
| `electorama-msvc-followers` | 9002 | `followers` |
| `electorama-msvc-geography` | 9003 | `geography` |
| `electorama-msvc-elections` | 9004 | `elections` |
| `electorama-msvc-logfile` | 9005 | `logfile` |
| `electorama-msvc-visits` | 9006 | `visits` |
| `electorama-msvc-canvasandwalls` | 9007 | `canvas_and_walls` |
| `electorama-msvc-citizen-issues` | 9008 | `citizen_issues` |
| `electorama-msvc-elections-results` | 9009 | `elections_results` |
| `electorama-msvc-elections-structure` | 9010 | `elections_structure` |
| `electorama-msvc-document-management` | 9011 | `document_management` |
| `electorama-msvc-polls` | 9012 | `polls` |
| `electorama-msvc-survey` | 9013 | `survey` |
| `electorama-msvc-structure` | 9014 | `structure` |
| `electorama-msvc-electoral-history` | 9015 | `electoral_history` |

```bash
set -a; . ../.env; set +a                     # variables de entorno (ver ../.env.example)
mvn -q spotless:apply                         # el check de formato corre en compile
mvn -q -Dmaven.test.skip=true install
java -jar electorama-msvc-<servicio>/target/*.jar
```

Ningún `application.yml` trae credenciales: todo secreto entra por variable de entorno.
El schema de la base lo manda Flyway (`../infra/db/migrations`); los servicios no crean tablas.
