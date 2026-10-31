import { ChangeDetectionStrategy, Component, DestroyRef, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { RouterOutlet } from '@angular/router';
import { catchError, of, switchMap } from 'rxjs';
import { OrganizationService } from '../data/organization.service';
import { CampaignSummary } from '../data/organization.types';
import { WorkingCampaign } from '../data/working-campaign';

const STATUS: Record<string, string> = { ACTIVE: 'activa', PLANNING: 'en planeación', CLOSED: 'cerrada' };

/**
 * La campaña de trabajo de todo el módulo de estructura. Cambiarla aquí vuelve a armar la pantalla
 * abierta (el `@for` con `version` recrea el `router-outlet`), así cada vista lee la campaña nueva
 * sin tener que escuchar el cambio. Si una vista la cambia por su cuenta (Campañas, al elegir una),
 * no se recrea nada.
 */
@Component({
  selector: 'app-campaign-bar',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterOutlet],
  template: `
    @if (campaigns().length > 1) {
      <div class="flex items-center justify-end px-6 pt-4 sm:px-10">
        <label class="flex items-center gap-2 text-sm">
          <span class="text-neutral-500">Campaña de trabajo</span>
          <select
            class="min-w-56 rounded-lg border border-neutral-200 bg-card px-3 py-2 text-sm dark:border-neutral-800"
            aria-label="Campaña de trabajo"
            (change)="select(+$any($event.target).value)"
          >
            @for (c of campaigns(); track c.id) {
              <option [value]="c.id" [selected]="c.id === working.campaign()?.id">
                {{ c.name }} · {{ status(c) }}
              </option>
            }
          </select>
        </label>
      </div>
    }
    @if (ready()) {
      @for (key of [version()]; track key) {
        <router-outlet />
      }
    }
  `,
})
export default class CampaignBar {
  protected working = inject(WorkingCampaign);
  private organization = inject(OrganizationService);
  private destroyRef = inject(DestroyRef);

  protected campaigns = signal<CampaignSummary[]>([]);
  /** Las vistas se arman cuando ya se sabe la campaña de trabajo. */
  protected ready = signal(false);
  protected version = signal(0);

  constructor() {
    this.working
      .load()
      .pipe(
        switchMap(() => {
          this.ready.set(true);
          return this.organization.getCampaigns();
        }),
        catchError(() => of([] as CampaignSummary[])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((list) =>
        // las activas primero, luego en planeación y al final las cerradas (sólo consulta)
        this.campaigns.set(
          [...list].sort(
            (a, b) => order(a.status) - order(b.status) || b.id - a.id
          )
        )
      );
  }

  protected status(c: CampaignSummary): string {
    return STATUS[c.status] ?? c.status;
  }

  protected select(id: number): void {
    const campaign = this.campaigns().find((c) => c.id === id) ?? null;
    if (campaign && campaign.id !== this.working.campaign()?.id) {
      this.working.select(campaign);
      this.version.update((v) => v + 1);
    }
  }
}

function order(status: string): number {
  return status === 'ACTIVE' ? 0 : status === 'PLANNING' ? 1 : 2;
}
