# Configuración de campaña y niveles de estructura (V3)

Cada campaña se configura en **Estructura › Campañas** (`/admin/structure/campaigns`, sólo ADMIN):

| Pestaña | Qué guarda | API (msvc-structure) |
|---|---|---|
| General | nombre, tipo (estatal/municipal/federal/interna), territorio, inicio, cierre, día de la elección, año, descripción; estatus PLANNING → ACTIVE → CLOSED | `POST /campaigns`, `PUT /campaigns/{id}`, `PATCH /campaigns/{id}/status` |
| Niveles | los niveles del organigrama, de la cúspide hacia abajo | `PUT /campaigns/{id}/levels` |
| Estructuras | Principal, Alterna, Aliada | `POST /campaigns/{id}/structures`, `PUT`/`DELETE /campaigns/structures/{id}` |
| Cargos | cargo, ámbito, cuántos, día | `PUT /campaigns/{id}/elections` |

Una campaña nueva nace en PLANNING con su estructura Principal. Sólo se activa con niveles y estructura, y
si no hay otra activa en el mismo territorio. Una cerrada ya no se edita.

## Territorio y meta (V4, V6)

- El territorio puede ser **cualquier estado** o uno de sus municipios: `structure.territory` tiene los
  32 estados (`infra/db/reference/territory-nacional.sql`). El código de una sección (`S-0815`) es
  único dentro de su estado.
- El estado de la campaña (`stateGeoId` en `CampaignSummary`) es el que usan el catálogo territorial,
  el organigrama, la captura y el histórico electoral. La pantalla trabaja con la **campaña de
  trabajo** (`structure/data/working-campaign.ts`): se elige en el selector **Campaña de trabajo**,
  arriba de todas las vistas de Estructura (`structure/ui/campaign-bar.ts`), y se recuerda en el
  navegador. Si no se ha elegido, es la activa. Cambiarla vuelve a cargar la vista abierta.
- **% de meta** (`campaign.goal_percentage`, 61 por default): se guarda con los datos generales y el
  botón *Aplicar a las secciones* lo copia a todas las secciones vigentes del territorio
  (`PATCH /api/geography/catalog/goal`), recalculando meta = LN × %. Después se puede seguir
  ajustando sección por sección en el catálogo territorial.

## Niveles (`structure.campaign_level`)

Cada nivel tiene:

- un **rango** (1 = cúspide). Un puesto cuelga de uno del nivel inmediato superior;
- un **nombre** en singular y plural;
- el **tipo de territorio** sobre el que va (estado, municipio, sección o "donde se elija");
- una **meta por puesto**, que también reparte las vacantes del nivel de arriba;
- un **rol de Keycloak** y, opcionalmente, uno alterno para la estructura alterna;
- opcionalmente, una **función** heredada.

La función conecta el nivel con lo que el sistema ya sabe hacer:

| Función | Qué trae |
|---|---|
| `REFERENT` (coordinación municipal) | uno por municipio, cabeza de rama |
| `ZONE_COORDINATOR` (promoción por sección) | clave de sección `S-0233A1`, meta por sección, «toda la sección» |
| `PROMOTER` (movilización) | clave `S-0233A1-01`, captura de convencidos |
| sin función | sólo agrega jerarquía (p.ej. un coordinador estatal o distrital) |

Cada función va a lo más una vez por campaña y en ese orden. Un nivel con puestos ya no cambia de rango,
territorio ni función, y no se quita; su nombre, meta y rol sí se editan. La base lo garantiza con
triggers (`campaign_level_in_use_guard`, `position_validate_hierarchy`).

Las campañas 2024 y 2027 quedaron con sus tres niveles (Coordinador › Promotor › Movilizador) y sus roles
de siempre, así que no cambia nada para sus usuarios.

## Roles

Al guardar los niveles, msvc-structure llama `POST /v2/roles/ensure` de msvc-users, que hace tres cosas:

1. crea el rol en el realm si falta;
2. lo registra en `users.structure_role` con su antigüedad (la cúspide de una campaña de N niveles tiene
   N), que es lo que usa msvc-users para decidir quién administra qué cuentas;
3. si el rol es nuevo, le copia los menús del rol de su función, si la tiene. Si no, se asignan en
   **Accesos**.

El rol por defecto es `<CAMPAÑA>_<CÓDIGO>`, p.ej. `TABASCO_2030_COORDINADOR_MUNICIPAL`.

## Alta de convencidos y del día de la elección

- **Convencidos:** se capturan en msvc-structure (`POST /memberships`): en el front desde
  *Estructura › Captura* y en la app desde su pantalla de captura (`/memberships/capture-targets`).
- **Ámbito de una cuenta en la app** (visitas, bardas): `GET /positions/scope/{authId}` y su superior
  con `GET /positions/holders/{authId}/superior`, ambos sobre la campaña activa.
- **Día de la elección:** msvc-followers sólo conserva la movilización, la marca de voto
  (`/followers/updateVoteFlag*`, `followersMark`) y el listado de promotores. Lee las vistas
  `followers.followers` / `followers.promoters`, que sólo muestran las membresías heredadas de 2024
  (`legacy_id`), y su marca de voto apunta a la campaña activa de la sección (V7,
  `structure.active_structure`). La capa compat de altas se quitó en V8.

## Pruebas

- **SQL:** `infra/db/tests/T8__campaign_levels.sql` y `T9__compat_campaign.sql` (`psql -d electorama -f …`,
  en `BEGIN … ROLLBACK`).
- **Back:**
  - `electorama-msvc-structure`: `LevelRulesTest` y `BuildServiceLevelsTest`.
  - `electorama-msvc-users`: `AccountServiceTest` y `ApiSecurityTest`.
- **Front:** `src/app/core/data/levels.spec.ts`.

## Pendiente (fase 2)

- **Textos fijos que todavía dicen "Promotores"/"Movilizadores" o "1 por cada 200/20":**
  - `campaign-dashboard`
  - `municipality-breakdown`
  - `supporters-analysis`
  - `territory-catalog`
  - `data/campaign-rules.ts`

  Se deben pasar a los nombres y metas de la campaña (`LevelCatalog`, `functionName`).
- **Reportes y tableros por nivel sin función** (p.ej. avance por coordinador distrital). Hoy esos puestos
  aparecen en el organigrama, pero los reportes agrupan por función.
- **Vacantes de niveles que no van sobre sección:** hoy se crean uno por uno.
- **Llamadas sin `campaignId`:** el tablero, el catálogo territorial, el desglose municipal, la captura y
  el listado de convencidos ya mandan la campaña de trabajo (y la cerrada de referencia es la del mismo
  estado). La vigilancia de secciones todavía no: cae en la ACTIVE más reciente de
  `CampaignService.resolve()`.
- **Tabasco fijo todavía** en el análisis de convencidos 2024 (`supporters-analysis`), la vigilancia de
  secciones (`section-watch`) y los mapas (`TABASCO_CENTER`, `cve_ent=27`).
