import { authInterceptor } from '@/app/core/auth/auth.interceptor';
import { loadingInterceptor } from '@/app/core/loading/loading.interceptor';
import { TranslocoHttpLoader } from '@/app/core/transloco/transloco-http-loader';
import {
  provideHttpClient,
  withFetch,
  withInterceptors,
  withJsonpSupport,
} from '@angular/common/http';
import {
  ApplicationConfig,
  isDevMode,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideNativeDateAdapter } from '@angular/material/core';
import { MAT_FORM_FIELD_DEFAULT_OPTIONS } from '@angular/material/form-field';
import {
  provideRouter,
  withComponentInputBinding,
  withInMemoryScrolling,
} from '@angular/router';
import { provideAppConfig } from '@/app/core/config/provider';
import { provideAccess } from '@/app/core/access/provider';
import { provideIcons } from '@/app/core/icons/provider';
import { provideTheming } from '@/app/core/theming';
import { provideTransloco } from '@jsverse/transloco';
import { routes } from './app.routes';

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideHttpClient(
      withFetch(),
      withInterceptors([authInterceptor, loadingInterceptor]),
      withJsonpSupport()
    ),
    provideRouter(
      routes,
      withComponentInputBinding(),
      withInMemoryScrolling({ scrollPositionRestoration: 'enabled' })
    ),

    // Material
    {
      provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
      useValue: {
        subscriptSizing: 'dynamic',
      },
    },
    provideNativeDateAdapter(),

    // Core
    provideAppConfig(),
    provideIcons(),
    // el menú y los permisos, resueltos antes del primer guard
    provideAccess(),
    provideTheming({
      scheme: 'light',
      primary: '#E8502E', // bermellón — color dominante de marca
      secondary: '#1E3A8A', // azul tinta — acento
      error: '#D11149', // carmesí, distinto del bermellón para acciones destructivas
    }),

    // Third-party
    provideTransloco({
      config: {
        availableLangs: [
          {
            id: 'en',
            label: 'English',
          },
          {
            id: 'es',
            label: 'Español',
          },
        ],
        defaultLang: 'es',
        reRenderOnLangChange: true,
        prodMode: !isDevMode(),
      },
      loader: TranslocoHttpLoader,
    }),
  ],
};
