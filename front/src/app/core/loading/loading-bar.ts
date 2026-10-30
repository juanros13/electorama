import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { LoadingService } from './loading.service';

/**
 * La barra del cargador global: una línea de marca pegada arriba de la ventana.
 *
 * Va en el componente raíz, así sale igual en el panel, el login y cualquier
 * layout. No bloquea clics: el trabajo en curso no debería impedir leer la
 * pantalla. Para acciones que sí hay que bloquear, el botón se deshabilita en
 * su propia pantalla.
 */
@Component({
  selector: 'app-loading-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: {
    class:
      'pointer-events-none fixed inset-x-0 top-0 z-[9999] h-[3px] overflow-hidden',
    role: 'progressbar',
    'aria-label': 'Cargando',
    '[attr.aria-hidden]': '!loading.active()',
    '[attr.aria-busy]': 'loading.active()',
  },
  template: `
    @if (loading.active()) {
      <div class="loading-bar__track bg-primary-100 dark:bg-primary-500/20">
        <div class="loading-bar__run bg-primary-500"></div>
      </div>
    }
  `,
  styles: `
    .loading-bar__track {
      position: relative;
      height: 100%;
      width: 100%;
    }
    .loading-bar__run {
      position: absolute;
      inset-block: 0;
      left: 0;
      width: 40%;
      border-radius: 9999px;
      box-shadow: 0 0 8px var(--color-primary-500);
      animation: loading-bar-run 1.1s cubic-bezier(0.65, 0, 0.35, 1) infinite;
    }
    @keyframes loading-bar-run {
      from {
        transform: translateX(-100%);
      }
      to {
        transform: translateX(250%);
      }
    }
    /* con "reducir movimiento" la barra se queda quieta y pulsa suave */
    @media (prefers-reduced-motion: reduce) {
      .loading-bar__run {
        width: 100%;
        animation: loading-bar-pulse 1.6s ease-in-out infinite;
      }
      @keyframes loading-bar-pulse {
        50% {
          opacity: 0.4;
        }
      }
    }
  `,
})
export class LoadingBar {
  protected loading = inject(LoadingService);
}
