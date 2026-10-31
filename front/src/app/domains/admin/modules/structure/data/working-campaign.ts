import { inject, Injectable, computed, signal } from '@angular/core';
import { Observable, catchError, map, of, shareReplay, tap } from 'rxjs';
import { DEFAULT_GOAL_PERCENTAGE } from './campaign-rules';
import { OrganizationService } from './organization.service';
import { CampaignSummary } from './organization.types';

const CAMPAIGN_KEY = 'structure.campaignId';

/** Tabasco: el estado de las campañas que ya existían, y el de respaldo si no hay campaña. */
export const FALLBACK_STATE_ID = 27;

/**
 * La campaña con la que se trabaja en el módulo de estructura: la que se eligió
 * en el organigrama (se recuerda en este navegador) o la activa. De ella sale el
 * estado que se consulta en geography y en el histórico, y su % de meta.
 */
@Injectable({ providedIn: 'root' })
export class WorkingCampaign {
  private organization = inject(OrganizationService);

  readonly campaign = signal<CampaignSummary | null>(null);
  readonly stateId = computed(() => this.campaign()?.stateGeoId ?? FALLBACK_STATE_ID);
  readonly goalPercentage = computed(
    () => this.campaign()?.goalPercentage ?? DEFAULT_GOAL_PERCENTAGE
  );

  private loaded$?: Observable<CampaignSummary | null>;

  /** Lee las campañas una vez y elige la de trabajo; las pantallas esperan esto antes de consultar. */
  load(): Observable<CampaignSummary | null> {
    this.loaded$ ??= this.organization.getCampaigns().pipe(
      map((list) => {
        const stored = storedId();
        return (
          list.find((c) => c.id === stored) ??
          list.find((c) => c.status === 'ACTIVE') ??
          list[0] ??
          null
        );
      }),
      catchError(() => of(null)),
      tap((c) => this.campaign.set(c)),
      shareReplay(1)
    );
    return this.loaded$;
  }

  select(campaign: CampaignSummary | null): void {
    this.campaign.set(campaign);
    try {
      if (campaign) {
        localStorage.setItem(CAMPAIGN_KEY, String(campaign.id));
      }
    } catch {
      // sin almacenamiento: sólo dura la sesión
    }
  }

  /** Después de crear o editar campañas: la próxima lectura vuelve a pedirlas. */
  refresh(updated?: CampaignSummary): void {
    this.loaded$ = undefined;
    if (updated && updated.id === this.campaign()?.id) {
      this.campaign.set(updated);
    }
  }
}

function storedId(): number | null {
  try {
    const value = Number(localStorage.getItem(CAMPAIGN_KEY));
    return value > 0 ? value : null;
  } catch {
    return null;
  }
}
