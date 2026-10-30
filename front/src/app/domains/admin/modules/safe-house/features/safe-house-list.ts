import { RolType } from '@/app/core/data/enum/RolType';
import { UserService } from '@/app/core/user/user.service';
import {
  closeLoading,
  confirmDialog,
  showError,
  showLoading,
  showSuccess,
} from '@/app/shared/ui/swal';
import { PageHeader } from '@/app/shared/ui/page-header';
import { TableShell } from '@/app/shared/ui/table-shell';
import {
  afterNextRender,
  AfterViewInit,
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
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginator, MatPaginatorModule } from '@angular/material/paginator';
import { MatSort, MatSortModule } from '@angular/material/sort';
import { MatTableDataSource, MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import * as L from 'leaflet';
import { catchError, of } from 'rxjs';
import { SafeHouseService } from '../data/safe-house.service';
import { SafeHouse } from '../data/safe-house.types';

const TABASCO_CENTER: L.LatLngTuple = [17.97103, -92.595953];

@Component({
  selector: 'app-safe-house-list',
  changeDetection: ChangeDetectionStrategy.OnPush,
  templateUrl: './safe-house-list.html',
  imports: [
    PageHeader,
    TableShell,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatIconModule,
    MatButtonModule,
    MatTooltipModule,
  ],
})
export default class SafeHouseList implements AfterViewInit {
  private service = inject(SafeHouseService);
  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);

  private readonly mapEl = viewChild.required<ElementRef<HTMLElement>>('mapEl');
  protected readonly paginator = viewChild.required(MatPaginator);
  protected readonly sort = viewChild.required(MatSort);

  protected readonly displayedColumns = [
    'alias',
    'manager',
    'managerphone',
    'reference',
    'actions',
  ];
  protected readonly dataSource = new MatTableDataSource<SafeHouse>([]);
  protected loading = signal(true);
  protected isAdmin = computed(() => this.userService.hasRole(RolType.ADMIN));

  private map?: L.Map;
  private markers = L.layerGroup();

  constructor() {
    afterNextRender(() => this.initMap());
    this.destroyRef.onDestroy(() => this.map?.remove());
    this.load();
  }

  ngAfterViewInit(): void {
    this.dataSource.paginator = this.paginator();
    this.dataSource.sort = this.sort();
  }

  async remove(house: SafeHouse): Promise<void> {
    const ok = await confirmDialog(
      '¿Estás seguro?',
      `¿Eliminar la casa segura "${house.alias}"?`
    );
    if (!ok) {
      return;
    }
    showLoading('Eliminando…');
    this.service.deleteSafeHouse(house).subscribe({
      next: () => {
        closeLoading();
        showSuccess('¡Proceso exitoso!', 'Se eliminó la casa segura.');
        this.dataSource.data = this.dataSource.data.filter(
          (h) => h.id !== house.id
        );
        this.renderMarkers();
      },
      error: () => {
        closeLoading();
        showError();
      },
    });
  }

  private load(): void {
    this.loading.set(true);
    this.service
      .getSafeHouses()
      .pipe(
        catchError(() => of([])),
        takeUntilDestroyed(this.destroyRef)
      )
      .subscribe((houses) => {
        this.loading.set(false);
        this.dataSource.data = houses ?? [];
        this.renderMarkers();
      });
  }

  private initMap(): void {
    const map = L.map(this.mapEl().nativeElement).setView(TABASCO_CENTER, 9);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      attribution:
        '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>',
      maxZoom: 19,
    }).addTo(map);
    this.markers.addTo(map);
    this.map = map;
    this.renderMarkers();
  }

  private renderMarkers(): void {
    if (!this.map) {
      return;
    }
    this.markers.clearLayers();
    const points: L.LatLngTuple[] = [];
    for (const house of this.dataSource.data) {
      const lat = Number(house.latitude);
      const lng = Number(house.longitude);
      if (!lat || !lng) {
        continue;
      }
      points.push([lat, lng]);
      L.circleMarker([lat, lng], {
        radius: 8,
        color: '#1565C0',
        fillColor: '#1565C0',
        fillOpacity: 0.7,
      })
        .bindPopup(
          `<b>${house.alias}</b><br>${house.managerName} · ${house.managerPhone}`
        )
        .addTo(this.markers);
    }
    if (points.length) {
      this.map.fitBounds(points, { maxZoom: 15, padding: [30, 30] });
    }
  }
}
