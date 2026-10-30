import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  ElementRef,
  effect,
  inject,
  input,
  output,
  signal,
  viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';

const TABASCO_CENTER: L.LatLngTuple = [17.97103, -92.595953];

export interface LatLng {
  lat: number;
  lng: number;
}

/**
 * Mapa de selección de ubicación para una persona visitada. Cada fila del
 * FormArray monta su propia instancia (evita el malabar de arrays indexados
 * del A15). Click / arrastre del marcador / geolocalización emiten lat-lng.
 */
@Component({
  selector: 'app-visit-location-map',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatIconModule],
  template: `
    <div class="flex flex-col gap-2">
      <button matButton="tonal" type="button" (click)="locateMe()">
        <mat-icon svgIcon="map-pin" />
        Obtener ubicación actual
      </button>
      <div #mapEl class="h-80 w-full rounded-lg border border-neutral-200 dark:border-neutral-800"></div>
    </div>
  `,
})
export class VisitLocationMap {
  /** Polígono GeoJSON de municipios (compartido, se dibuja una vez). */
  municipalities = input<any | null>(null);
  /** Polígono GeoJSON de la sección seleccionada. */
  section = input<any | null>(null);
  /** Coordenadas iniciales (modo edición). */
  initialLat = input<number | null>(null);
  initialLng = input<number | null>(null);

  locationChange = output<LatLng>();

  private destroyRef = inject(DestroyRef);
  private mapEl = viewChild.required<ElementRef<HTMLElement>>('mapEl');

  private map?: L.Map;
  private marker?: L.Marker;
  private municipalitiesLayer?: L.GeoJSON;
  private sectionLayer?: L.GeoJSON;
  private mapReady = signal(false);

  private readonly icon = L.icon({
    iconUrl: '/assets/icons/marker.png',
    iconSize: [32, 32],
    iconAnchor: [16, 32],
  });

  constructor() {
    afterNextRender(() => this.initMap());
    this.destroyRef.onDestroy(() => this.map?.remove());

    // Dibuja municipios cuando llegan (una sola vez).
    effect(() => {
      const data = this.municipalities();
      if (this.mapReady() && data && !this.municipalitiesLayer) {
        this.municipalitiesLayer = L.geoJSON(data, {
          style: { weight: 1, fillOpacity: 0, color: '#746d6d' },
        }).addTo(this.map!);
      }
    });

    // Redibuja la sección y centra cuando cambia.
    effect(() => {
      const data = this.section();
      if (!this.mapReady() || !data) {
        return;
      }
      if (this.sectionLayer) {
        this.map!.removeLayer(this.sectionLayer);
      }
      this.sectionLayer = L.geoJSON(data, {
        style: { weight: 3, opacity: 1, color: '#337397', fillOpacity: 0.2, fillColor: '#71adc9' },
      }).addTo(this.map!);
      try {
        const bounds = this.sectionLayer.getBounds();
        this.map!.fitBounds(bounds);
        if (!this.marker) {
          const center = bounds.getCenter();
          this.placeMarker([center.lat, center.lng]);
        }
      } catch {
        /* sección sin geometría: conservar vista */
      }
    });
  }

  private initMap(): void {
    const map = L.map(this.mapEl().nativeElement).setView(TABASCO_CENTER, 9);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);

    map.on('click', (e: L.LeafletMouseEvent) => this.placeMarker([e.latlng.lat, e.latlng.lng]));

    this.map = map;
    this.mapReady.set(true);

    const lat = this.initialLat();
    const lng = this.initialLng();
    if (lat != null && lng != null) {
      this.placeMarker([lat, lng]);
      map.setView([lat, lng], 15);
    }
  }

  private placeMarker(location: L.LatLngTuple): void {
    if (!this.map) {
      return;
    }
    if (this.marker) {
      this.marker.setLatLng(location);
    } else {
      this.marker = L.marker(location, { draggable: true, autoPan: true, icon: this.icon }).addTo(this.map);
      this.marker.on('dragend', (e: L.LeafletEvent) => {
        const pos = (e.target as L.Marker).getLatLng();
        this.emit(pos.lat, pos.lng);
      });
    }
    this.map.setView(location);
    this.emit(location[0], location[1]);
  }

  private emit(lat: number, lng: number): void {
    this.locationChange.emit({ lat, lng });
  }

  locateMe(): void {
    if (!navigator.geolocation) {
      return;
    }
    navigator.geolocation.getCurrentPosition((pos) => {
      this.placeMarker([pos.coords.latitude, pos.coords.longitude]);
      this.map?.setView([pos.coords.latitude, pos.coords.longitude], 16);
    });
  }
}
