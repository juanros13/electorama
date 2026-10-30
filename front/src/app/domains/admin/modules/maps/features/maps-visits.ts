import { AppConfigService } from '@/app/core/config/app-config';
import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import * as L from 'leaflet';
import 'leaflet.markercluster';
import { catchError, of } from 'rxjs';
import { VisitsMapService } from '../data/visits-map.service';

const TABASCO_CENTER: L.LatLngTuple = [17.97103, -92.595953];

@Component({
  selector: 'app-maps-visits',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="relative flex flex-auto flex-col">
      @if (loading()) {
        <div
          class="absolute top-3 left-1/2 z-[1000] -translate-x-1/2 rounded-full bg-white px-4 py-1.5 text-sm shadow dark:bg-neutral-800"
        >
          Cargando visitas…
        </div>
      }
      <div #mapEl class="h-[calc(100vh-7.5rem)] w-full"></div>
    </div>
  `,
})
export default class MapsVisits {
  private service = inject(VisitsMapService);
  private appConfig = inject(AppConfigService);
  private destroyRef = inject(DestroyRef);

  private readonly mapEl = viewChild.required<ElementRef<HTMLElement>>('mapEl');
  private map?: L.Map;

  protected loading = signal(true);

  constructor() {
    afterNextRender(() => this.initMap());
    this.destroyRef.onDestroy(() => this.map?.remove());
  }

  private initMap(): void {
    const map = L.map(this.mapEl().nativeElement).setView(TABASCO_CENTER, 9);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);

    // Capa WMS de municipios (GeoServer).
    L.tileLayer
      .wms(`${this.appConfig.config.url_geoserver}?CQL_FILTER=cve_ent=27`, {
        layers: 'serveros:geo_municipios_tab',
        format: 'image/png',
        transparent: true,
        version: '1.1.0',
        opacity: 0.6,
      })
      .addTo(map);

    this.map = map;
    this.loadVisits();
  }

  private loadVisits(): void {
    this.service
      .getReportVisitMap()
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((visits) => {
        this.loading.set(false);
        if (!this.map || !visits?.length) {
          return;
        }
        const cluster = (L as any).markerClusterGroup();
        for (const v of visits) {
          // El backend entrega lat/lng con nombres invertidos (igual que el viejo).
          const marker = L.marker(L.latLng(v.longitude, v.latitude));
          cluster.addLayer(marker);
        }
        this.map.addLayer(cluster);
      });
  }
}
