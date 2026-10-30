import {
  afterNextRender,
  ChangeDetectionStrategy,
  Component,
  computed,
  DestroyRef,
  ElementRef,
  inject,
  signal,
  viewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DecimalPipe } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { PageHeader } from '@/app/shared/ui/page-header';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import * as L from 'leaflet';
import 'leaflet.markercluster';
import { catchError, of } from 'rxjs';
import { SurveysReportService } from '../data/surveys-report.service';
import { GetSurveyReportMunicipalityLocation, MunStat } from '../data/surveys-report.types';

const TABASCO_CENTER: L.LatLngTuple = [17.97103, -92.595953];

@Component({
  selector: 'app-state-report',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './state-report.html',
  imports: [DecimalPipe, PageHeader, MatButtonModule, MatIconModule],
})
export default class StateReport {
  private service = inject(SurveysReportService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  private mapEl = viewChild.required<ElementRef<HTMLElement>>('mapEl');
  private map?: L.Map;
  private cluster = (L as any).markerClusterGroup();
  private locations: GetSurveyReportMunicipalityLocation[] = [];

  protected munName = signal('');
  protected stat = signal<MunStat | null>(null);
  protected loading = signal(true);

  private readonly icon = L.icon({ iconUrl: '/assets/icons/safe_house.png', iconSize: [32, 32], iconAnchor: [16, 32] });

  protected percent = computed(() => {
    const s = this.stat();
    return s?.goalHolmes ? Math.round((s.formCaptured * 100) / s.goalHolmes) : 0;
  });

  constructor() {
    this.munName.set(this.route.snapshot.paramMap.get('name') ?? '');
    const id = this.route.snapshot.paramMap.get('id');

    afterNextRender(() => this.initMap());
    this.destroyRef.onDestroy(() => this.map?.remove());

    if (id) {
      this.service
        .getMunProgressDetail(id)
        .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
        .subscribe((locs) => {
          this.locations = locs ?? [];
          this.loading.set(false);
          this.renderMarkers();
        });

      this.service
        .getSurveyMunicipalityDetail(id)
        .pipe(catchError(() => of([])), takeUntilDestroyed(this.destroyRef))
        .subscribe((stats) => this.stat.set(stats?.[0] ?? null));
    }
  }

  back(): void {
    this.router.navigate(['/admin/surveys/reports/municipalitiesprogress']);
  }

  private initMap(): void {
    const map = L.map(this.mapEl().nativeElement).setView(TABASCO_CENTER, 9);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);
    map.addLayer(this.cluster);
    this.map = map;
    this.renderMarkers();
  }

  private renderMarkers(): void {
    if (!this.map || !this.locations.length) {
      return;
    }
    this.cluster.clearLayers();
    for (const h of this.locations) {
      this.cluster.addLayer(L.marker(L.latLng(Number(h.latitude), Number(h.longitude)), { icon: this.icon }));
    }
    const first = this.locations[0];
    this.map.setView([Number(first.latitude), Number(first.longitude)], 11);
  }
}
