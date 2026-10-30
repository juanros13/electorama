/**
 * Configuración de runtime cargada desde `public/config.json` al arrancar la app.
 *
 * Las URLs y los settings de Keycloak NO se hornean en el bundle: viven en
 * `config.json` (servido como asset) para que el mismo build sirva en
 * dev/staging/prod. El secret de Keycloak se inyecta en deploy / local y no se
 * versiona con su valor real (ver `docs/migration/00-inventario-secrets-urls.md`).
 *
 * TODO(PKCE): cuando Ops convierta `electorama-front` a cliente público con
 * PKCE, `keycloak_secret` desaparece y el flujo ROPC se reemplaza por code flow.
 */
export interface AppConfig {
  production: boolean;

  // Keycloak
  url_keycloak: string;
  keycloak_realm: string;
  keycloak_client: string;
  keycloak_secret: string;

  // Gateway / microservicios
  url_user: string;
  url_user_profile: string;
  url_calendar: string;
  url_auth_google: string;
  url_geo_elections: string;
  url_geography: string;
  url_followers: string;
  /** Modelo de estructura con puestos y vigencias (msvc-structure). */
  url_structure: string;
  /** msvc-electoral-history: resultados oficiales 2018/2021/2024. */
  url_electoral_history: string;
  url_visits: string;
  url_canvas_anda_walls: string;
  url_citizen_issues: string;
  url_surveys: string;
  url_coordinators: string;
  url_survey_catalogs: string;
  url_survey_sections: string;
  url_survey_annex: string;
  url_survey_report: string;
  url_survey_summary: string;
  url_personEntity: string;
  url_log: string;
  url_cedula: string;
  url_validacion: string;

  // GeoServer
  url_geoserver: string;
  url_geoserver_ows: string;
}
