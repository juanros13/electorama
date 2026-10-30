import { AppConfig } from '@/app/core/config/app-config.types';
import { Injectable, signal } from '@angular/core';

/**
 * Mantiene la configuración de runtime cargada desde `config.json`.
 *
 * Se llena una sola vez durante `provideAppConfig()` (app initializer), antes de
 * que cualquier feature la consuma. Los servicios deben leer `config` dentro de
 * sus métodos (no en el constructor) para garantizar que ya esté cargada.
 */
@Injectable({ providedIn: 'root' })
export class AppConfigService {
  private readonly _config = signal<AppConfig | null>(null);

  /** Configuración cargada. Lanza si se accede antes del app initializer. */
  get config(): AppConfig {
    const config = this._config();
    if (!config) {
      throw new Error(
        'AppConfig no cargada todavía. ¿Falta provideAppConfig() o se accedió antes del app initializer?'
      );
    }
    return config;
  }

  /** Señal de solo lectura, útil para plantillas/computed. */
  readonly config$ = this._config.asReadonly();

  private _resolveReady!: (config: AppConfig) => void;

  /**
   * Se resuelve cuando `config.json` ya está cargada. Angular corre los app
   * initializers en paralelo, así que otro initializer que necesite la config
   * tiene que esperar esta promesa en vez de leer `config` directo.
   */
  readonly ready: Promise<AppConfig> = new Promise(
    (resolve) => (this._resolveReady = resolve)
  );

  set(config: AppConfig): void {
    this._config.set(config);
    this._resolveReady(config);
  }

  /** URL del endpoint de token de Keycloak, derivada de base + realm. */
  get tokenUrl(): string {
    const { url_keycloak, keycloak_realm } = this.config;
    return `${url_keycloak}/realms/${keycloak_realm}/protocol/openid-connect/token`;
  }
}
