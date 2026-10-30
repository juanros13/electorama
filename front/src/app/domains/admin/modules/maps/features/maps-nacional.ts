import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
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
import { catchError, of } from 'rxjs';

// Centro aproximado de Tabasco.
const TABASCO_CENTER: L.LatLngTuple = [17.97103, -92.595953];

@Component({
  selector: 'app-maps-nacional',
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="relative flex flex-auto flex-col">
      @if (loading()) {
        <div
          class="absolute top-3 left-1/2 z-[1000] -translate-x-1/2 rounded-full bg-white px-4 py-1.5 text-sm shadow dark:bg-neutral-800"
        >
          Cargando mapa…
        </div>
      }
      <div #mapEl class="h-[calc(100vh-7.5rem)] w-full"></div>
    </div>
  `,
})
export default class MapsNacional {
  private http = inject(HttpClient);
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
    this.map = map;
    this.loadMunicipalities();
  }

  /** Capa WFS (GeoJSON) de municipios de Tabasco desde GeoServer. */
  private loadMunicipalities(): void {
    const url =
      `${this.appConfig.config.url_geoserver_ows}?service=WFS&CQL_FILTER=cve_ent=27` +
      `&version=1.0.0&request=GetFeature&typeName=serveros%3Ageo_municipios_tab` +
      `&outputFormat=application%2Fjson`;

    this.http
      .get<any>(url)
      .pipe(
        catchError(() => of(null)),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((geojson) => {
        this.loading.set(false);
        if (!geojson || !this.map) {
          return;
        }
        const layer = L.geoJSON(geojson, {
          style: {
            color: '#1565C0',
            weight: 1,
            fillColor: '#1565C0',
            fillOpacity: 0.08,
          },
        }).addTo(this.map);
        try {
          this.map.fitBounds(layer.getBounds());
        } catch {
          /* capa vacía: conservar el centro por defecto */
        }
      });
  }
}
