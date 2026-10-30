import { AppConfigService } from '@/app/core/config/app-config';
import { AppConfig } from '@/app/core/config/app-config.types';
import { HttpClient } from '@angular/common/http';
import {
  EnvironmentProviders,
  inject,
  makeEnvironmentProviders,
  provideAppInitializer,
} from '@angular/core';
import { firstValueFrom } from 'rxjs';

/**
 * Carga `config.json` antes de bootstrap y lo deja disponible en
 * {@link AppConfigService}. Así las URLs/settings no se hornean en el bundle.
 */
export const provideAppConfig = (): EnvironmentProviders =>
  makeEnvironmentProviders([
    provideAppInitializer(() => {
      const http = inject(HttpClient);
      const appConfig = inject(AppConfigService);

      return firstValueFrom(http.get<AppConfig>('/config.json')).then(
        (config) => appConfig.set(config)
      );
    }),
  ]);
