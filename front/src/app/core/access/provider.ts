import {
  EnvironmentProviders,
  inject,
  makeEnvironmentProviders,
  provideAppInitializer,
} from '@angular/core';
import { AppConfigService } from '@/app/core/config/app-config';
import { firstValueFrom } from 'rxjs';
import { AccessService } from './access.service';

/**
 * Trae el menú de msvc-users antes de que corra el primer guard.
 *
 * Si se pidiera al pintar el menú, la primera navegación decidiría con el mapa
 * de código y la segunda con el de la base: dos respuestas distintas a la misma
 * pregunta, que es justo lo que no se puede permitir en permisos.
 *
 * No bloquea el arranque si falla: sin sesión —la pantalla de login— la
 * petición ni se hace, y con el servicio caído se usa el respaldo en código.
 *
 * Espera a `config.json`: los initializers corren en paralelo y la URL de
 * msvc-users sale de ahí.
 */
export const provideAccess = (): EnvironmentProviders =>
  makeEnvironmentProviders([
    provideAppInitializer(() => {
      const access = inject(AccessService);
      return inject(AppConfigService).ready.then(() =>
        firstValueFrom(access.loadMenu())
      );
    }),
  ]);
