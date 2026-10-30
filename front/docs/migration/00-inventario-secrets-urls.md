# Inventario de secrets / URLs y plan de externalización

> Fase 0 de la migración a Fuse 21 / Angular 21. Fuente: `nucleo-dash-front/src/environments/environment.ts` (proyecto A15) y su uso en `core/auth`.

## 1. Secretos expuestos en el bundle (CRÍTICO)

### 1.1 `keycloak_secret` (REDACTADO — valor real en `environment.ts` del repo viejo)
- **Uso:** `core/auth/auth.service.ts` en `signIn()` (L98-99), `checkPassword()` (L156-157) y `signInUsingToken()` (L178-180).
- **Flujo actual:** OAuth2 **Resource Owner Password Credentials (ROPC)** — `grant_type=password` + `client_id=nucleo-front-client` + `client_secret`, contra `https://auth.nucleo.rocks/realms/nucleo-dash-realm/protocol/openid-connect/token`. El refresh usa `grant_type=refresh_token` con el mismo secret.
- **Problema:** un client_secret de cliente confidencial viaja en el bundle JS público → cualquiera lo extrae. Además ROPC está **deprecado** (OAuth 2.1).
- **Plan:** convertir `nucleo-front-client` a **cliente público con PKCE** (Authorization Code + PKCE) en Keycloak; eliminar `client_secret` del frontend. Requiere:
  - **Ops/Keycloak:** marcar el client como público, habilitar Standard Flow + PKCE (S256), configurar redirect URIs.
  - **Frontend (Fase 1):** reescribir `auth.service` al code flow (lib recomendada `angular-oauth2-oidc` o el soporte OIDC nativo) en vez de ROPC.
  - **ROTAR** el secret ya expuesto en git history del proyecto viejo, sí o sí.

### 1.2 `geoserver_key` (REDACTADO — valor real en `environment.ts` del repo viejo)
- **Decodificado (base64):** `admin:****` → **usuario y contraseña de administrador de GeoServer** en Basic auth (valor real no se reproduce aquí a propósito).
- **Uso:** SOLO en una línea **comentada** de `core/auth/auth.interceptor.ts` (L51-53). En runtime **no se usa hoy** (las peticiones a `geoserver.nucleo.rocks` salen sin Authorization).
- **Problema:** credenciales admin embebidas en el bundle, aunque estén comentadas. Riesgo crítico.
- **Plan:** **no portar** la constante. Si GeoServer requiere auth, **proxyar por backend** (nunca admin creds en cliente). **ROTAR la contraseña de admin de GeoServer** (expuesta en git history).

## 2. URLs de API (no secretas, pero a externalizar)

Todas apuntan a producción `https://gateway.nucleo.rocks/api/*` (con variantes staging `back.nucleostaging.net` y local `192.168.1.150:8090` comentadas). Dominios: `user`, `calendar`, `geoserver`(wms/ows), `elections`, `geography`, `followers`, `visits`, `canvasAndWalls`, `citizen-issues`, `survey` (+ catalogs/sections/annex/report/summary), `logfile`. Keycloak: `auth.nucleo.rocks`, realm `nucleo-dash-realm`.

- **Plan:** cargar URLs desde `public/config.json` en el arranque (`provideAppInitializer`) para que el **mismo build** sirva en dev/staging/prod sin recompilar y sin hornear URLs en el bundle. Alternativa mínima: mantener `environment.*.ts` pero **sin secretos**.

## 3. Otros hallazgos de auth a corregir en el port

- **Rol frágil:** `userTemp['rol'] = token_decode.realm_access.roles[3]` — rol por índice fijo del array. Reemplazar por mapeo explícito contra `RolType`.
- **Tokens en `localStorage`** (access + refresh) → expuestos a XSS. Evaluar en Fase 1 (al menos no guardar refresh; con code flow + PKCE el manejo cambia).
- **Logging de IP** vía `api64.ipify.org` en cada login (`IpService`) — request externo en cada acción logueada; revisar necesidad/privacidad.
- **Interceptor:** caso `ngrok-free.app` (header `Ngrok-skip-browser-warning`) es de desarrollo — no portar a prod.

## 4. Checklist de acción

- [ ] **Ops:** rotar `keycloak_secret` y password admin de GeoServer (ambos en git history del repo viejo).
- [ ] **Ops/Keycloak:** convertir `nucleo-front-client` a público + PKCE (Standard Flow, S256, redirect URIs).
- [ ] **Fase 1:** reescribir `auth.service` a Authorization Code + PKCE (sin client_secret).
- [ ] **Fase 1:** no portar `geoserver_key`; proxyar GeoServer por backend si requiere auth.
- [ ] **Fase 1:** mover URLs a `public/config.json` + `provideAppInitializer`.
- [ ] **Fase 1:** mapear rol por `RolType` explícito; revisar almacenamiento de tokens.
