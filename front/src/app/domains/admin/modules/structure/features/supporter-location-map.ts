import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  DestroyRef,
  effect,
  ElementRef,
  inject,
  input,
  output,
  signal,
  untracked,
  viewChild,
} from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import type { GeoJsonObject, Geometry } from 'geojson';
import * as L from 'leaflet';

const TABASCO_CENTER: L.LatLngTuple = [17.97103, -92.595953];

/** El margen del GPS: un punto a esta distancia del borde cuenta como dentro (igual que el servidor). */
export const SECTION_TOLERANCE_METERS = 100;

export interface GeoPoint {
  lat: number;
  lng: number;
}

/** Un convencido ya capturado, para pintarlo en el mapa. */
export interface MapSupporter extends GeoPoint {
  name: string;
}

type LocateState = 'idle' | 'locating' | 'denied' | 'unavailable' | 'outside';

/**
 * El mapa de la captura: marca dónde vive el convencido (GPS del dispositivo,
 * clic o arrastre del marcador) y enseña a los que ya se capturaron en el
 * puesto. Con la sección dibujada, sólo acepta puntos dentro de ella (el
 * servidor lo vuelve a revisar con PostGIS). El valor lo manda la pantalla: al guardar lo pone en null y el
 * marcador desaparece para el siguiente.
 */
@Component({
  selector: 'app-supporter-location-map',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [MatButtonModule, MatIconModule],
  template: `
    <div class="flex flex-col gap-2">
      <div class="flex flex-wrap items-center gap-3">
        <button matButton="tonal" type="button" [disabled]="locating() === 'locating'" (click)="locate()">
          <mat-icon svgIcon="locate-fixed" class="icon-size-4" />
          {{ locating() === 'locating' ? 'Buscando…' : 'Usar mi ubicación' }}
        </button>
        @if (value(); as v) {
          <span class="font-mono text-xs text-neutral-500 tabular-nums">
            {{ v.lat.toFixed(6) }}, {{ v.lng.toFixed(6) }}
            @if (accuracy(); as a) {
              <span class="text-neutral-400">· ±{{ a }} m</span>
            }
          </span>
          <button matButton type="button" class="!text-xs" (click)="clear()">Quitar</button>
        } @else {
          <span class="text-xs text-neutral-500">Sin ubicación: usa tu GPS o haz clic en el mapa.</span>
        }
      </div>
      @if (locating() === 'outside') {
        <p class="text-xs text-amber-700 dark:text-amber-400">
          Ese punto queda a más de {{ tolerance }} m de la sección. Marca el domicilio dentro del área
          sombreada.
        </p>
      } @else if (locating() === 'denied') {
        <p class="text-xs text-amber-700 dark:text-amber-400">
          El navegador no dio permiso de ubicación. Actívalo para este sitio o marca el punto en el mapa.
        </p>
      } @else if (locating() === 'unavailable') {
        <p class="text-xs text-amber-700 dark:text-amber-400">
          No se pudo obtener la ubicación. Marca el punto en el mapa.
        </p>
      }
      <div #mapEl class="h-72 w-full rounded-lg border border-neutral-200 dark:border-neutral-800"></div>
      <div class="flex items-center gap-4 text-2xs text-neutral-500">
        <span class="flex items-center gap-1.5">
          <span class="size-3 rounded-full border-2 border-white bg-primary-600 shadow"></span> este convencido
        </span>
        <span class="flex items-center gap-1.5">
          <span class="size-2 rounded-full bg-secondary-600/70"></span> ya capturados
        </span>
      </div>
    </div>
  `,
})
export class SupporterLocationMap {
  /** El punto marcado; null sin ubicación. Lo controla la pantalla. */
  value = input<GeoPoint | null>(null);
  /** Los convencidos ya capturados del puesto. */
  supporters = input<MapSupporter[]>([]);
  /** El polígono de la sección: se dibuja y los puntos fuera se rechazan. */
  section = input<Geometry | null>(null);

  valueChange = output<GeoPoint | null>();

  protected locating = signal<LocateState>('idle');
  protected readonly tolerance = SECTION_TOLERANCE_METERS;
  protected accuracy = signal<number | null>(null);

  private destroyRef = inject(DestroyRef);
  private mapEl = viewChild.required<ElementRef<HTMLElement>>('mapEl');
  private map?: L.Map;
  private marker?: L.Marker;
  private supportersLayer = L.layerGroup();
  private sectionLayer?: L.GeoJSON;
  private ready = signal(false);

  private readonly pin = L.divIcon({
    className: '',
    html: '<div style="width:18px;height:18px;border-radius:9999px;background:#9D2449;border:3px solid #fff;box-shadow:0 1px 4px rgba(0,0,0,.4)"></div>',
    iconSize: [18, 18],
    iconAnchor: [9, 9],
  });

  constructor() {
    afterNextRender(() => this.init());
    this.destroyRef.onDestroy(() => this.map?.remove());

    // el marcador sigue al valor (al guardar, la pantalla lo pone en null)
    effect(() => {
      const v = this.value();
      if (!this.ready()) {
        return;
      }
      untracked(() => this.drawMarker(v));
    });

    effect(() => {
      const geometry = this.section();
      if (!this.ready()) {
        return;
      }
      untracked(() => this.drawSection(geometry));
    });

    effect(() => {
      const points = this.supporters();
      if (!this.ready()) {
        return;
      }
      untracked(() => this.drawSupporters(points));
    });
  }

  protected locate(): void {
    if (!navigator.geolocation) {
      this.locating.set('unavailable');
      return;
    }
    this.locating.set('locating');
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        const p = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        this.map?.setView([p.lat, p.lng], 17);
        if (!this.inside(p)) {
          this.locating.set('outside');
          return;
        }
        this.locating.set('idle');
        this.accuracy.set(Math.round(pos.coords.accuracy));
        this.set(p);
      },
      (err) => this.locating.set(err.code === err.PERMISSION_DENIED ? 'denied' : 'unavailable'),
      { enableHighAccuracy: true, timeout: 15000, maximumAge: 30000 }
    );
  }

  protected clear(): void {
    this.accuracy.set(null);
    this.valueChange.emit(null);
  }

  private set(p: GeoPoint): void {
    this.valueChange.emit({ lat: round(p.lat), lng: round(p.lng) });
  }

  /**
   * Si el punto cae en la sección o a no más de 100 m de su borde (el error
   * del GPS); sin polígono no se puede decir que no.
   */
  private inside(p: GeoPoint): boolean {
    const g = this.section();
    return !g || pointInGeometry(p, g) || distanceToGeometry(p, g) <= SECTION_TOLERANCE_METERS;
  }

  private init(): void {
    const map = L.map(this.mapEl().nativeElement).setView(TABASCO_CENTER, 9);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);
    this.supportersLayer.addTo(map);
    map.on('click', (e: L.LeafletMouseEvent) => {
      const p = { lat: e.latlng.lat, lng: e.latlng.lng };
      if (!this.inside(p)) {
        this.locating.set('outside');
        return;
      }
      // un clic es una corrección a mano: la precisión del GPS ya no aplica
      this.locating.set('idle');
      this.accuracy.set(null);
      this.set(p);
    });
    this.map = map;
    this.ready.set(true);
  }

  private drawMarker(v: GeoPoint | null): void {
    if (!this.map) {
      return;
    }
    if (!v) {
      this.marker?.remove();
      this.marker = undefined;
      return;
    }
    if (this.marker) {
      this.marker.setLatLng([v.lat, v.lng]);
    } else {
      this.marker = L.marker([v.lat, v.lng], { draggable: true, icon: this.pin, zIndexOffset: 1000 }).addTo(this.map);
      this.marker.on('dragend', (e: L.LeafletEvent) => {
        const ll = (e.target as L.Marker).getLatLng();
        const p = { lat: ll.lat, lng: ll.lng };
        if (!this.inside(p)) {
          // se regresa a donde estaba: fuera de la sección no se puede
          this.locating.set('outside');
          const v = this.value();
          if (v) {
            this.marker?.setLatLng([v.lat, v.lng]);
          }
          return;
        }
        this.locating.set('idle');
        this.accuracy.set(null);
        this.set(p);
      });
    }
  }

  private drawSection(geometry: Geometry | null): void {
    if (!this.map) {
      return;
    }
    this.sectionLayer?.remove();
    this.sectionLayer = undefined;
    if (!geometry) {
      return;
    }
    this.sectionLayer = L.geoJSON(geometry as GeoJsonObject, {
      style: { color: '#9D2449', weight: 2, fillColor: '#9D2449', fillOpacity: 0.08 },
      interactive: false,
    }).addTo(this.map);
    this.map.fitBounds(this.sectionLayer.getBounds(), { padding: [16, 16] });
  }

  private drawSupporters(points: MapSupporter[]): void {
    if (!this.map) {
      return;
    }
    this.supportersLayer.clearLayers();
    for (const p of points) {
      L.circleMarker([p.lat, p.lng], {
        radius: 5,
        weight: 1,
        color: '#ffffff',
        fillColor: '#1e3a5f',
        fillOpacity: 0.75,
      })
        .bindTooltip(p.name)
        .addTo(this.supportersLayer);
    }
    // si todavía no hay punto ni sección, se encuadra a lo ya capturado
    if (points.length && !this.marker && !this.sectionLayer) {
      this.map.fitBounds(L.latLngBounds(points.map((p) => [p.lat, p.lng] as L.LatLngTuple)), {
        maxZoom: 16,
        padding: [24, 24],
      });
    }
  }
}

/** Seis decimales: ~10 cm, de sobra para un domicilio. */
function round(n: number): number {
  return Math.round(n * 1e6) / 1e6;
}

/** Metros del punto al borde más cercano del Polygon o MultiPolygon. */
function distanceToGeometry(p: GeoPoint, g: Geometry): number {
  const polygons =
    g.type === 'Polygon' ? [g.coordinates] : g.type === 'MultiPolygon' ? g.coordinates : [];
  // proyección local en metros alrededor del punto: a escala de una sección el error es de cm
  const mPerLat = 111_320;
  const mPerLng = 111_320 * Math.cos((p.lat * Math.PI) / 180);
  const toXY = ([lng, lat]: number[]) => [(lng - p.lng) * mPerLng, (lat - p.lat) * mPerLat];
  let best = Infinity;
  for (const rings of polygons) {
    for (const ring of rings) {
      for (let i = 1; i < ring.length; i++) {
        best = Math.min(best, distanceToSegment(toXY(ring[i - 1]), toXY(ring[i])));
      }
    }
  }
  return best;
}

/** Distancia del origen (el punto) al segmento a–b, en las mismas unidades. */
function distanceToSegment([ax, ay]: number[], [bx, by]: number[]): number {
  const dx = bx - ax;
  const dy = by - ay;
  const len2 = dx * dx + dy * dy;
  const t = len2 === 0 ? 0 : Math.max(0, Math.min(1, -(ax * dx + ay * dy) / len2));
  return Math.hypot(ax + t * dx, ay + t * dy);
}

/** Punto dentro de un Polygon o MultiPolygon de GeoJSON (con huecos), por rayos. */
function pointInGeometry(p: GeoPoint, g: Geometry): boolean {
  const polygons =
    g.type === 'Polygon' ? [g.coordinates] : g.type === 'MultiPolygon' ? g.coordinates : [];
  return polygons.some(
    (rings) =>
      rings.length > 0 && inRing(p, rings[0]) && !rings.slice(1).some((hole) => inRing(p, hole))
  );
}

function inRing(p: GeoPoint, ring: number[][]): boolean {
  let inside = false;
  for (let i = 0, j = ring.length - 1; i < ring.length; j = i++) {
    const [xi, yi] = ring[i];
    const [xj, yj] = ring[j];
    if (yi > p.lat !== yj > p.lat && p.lng < ((xj - xi) * (p.lat - yi)) / (yj - yi) + xi) {
      inside = !inside;
    }
  }
  return inside;
}
