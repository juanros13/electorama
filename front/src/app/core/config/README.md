# Config de runtime

La app carga `public/config.json` al arrancar (`provideAppConfig()` → app initializer) y lo expone vía `AppConfigService`. Esto mantiene las URLs y los settings de Keycloak **fuera del bundle**, para que el mismo build sirva en dev/staging/prod.

## Uso en servicios

Leer la config **dentro de los métodos** (no en el constructor):

```ts
private appConfig = inject(AppConfigService);

getUsers() {
  return this.http.get(`${this.appConfig.config.url_user}/...`);
}
```

## El secret de Keycloak (temporal — flujo ROPC)

`keycloak_secret` va **vacío** en el `config.json` versionado. Mientras el auth use ROPC:

- **Local:** pega el secret real en tu `public/config.json` y **no lo commitees**.
- **Deploy:** el pipeline reemplaza `config.json` (o solo el secret) con el valor real.

Cuando Ops convierta `electorama-front` a cliente **público + PKCE**, el secret desaparece por completo y este campo se elimina. Ver `docs/migration/00-inventario-secrets-urls.md`.
