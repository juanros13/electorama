import type { Geometry } from 'geojson';
import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import {
  CatalogDistrict,
  CatalogMunicipality,
  CatalogPage,
  CatalogSection,
  CatalogSummary,
  SectionChange,
  SectionFilter,
  SectionLocality,
} from './territory.types';

/** Un filtro en null no viaja: del otro lado "ausente" es "sin filtrar". */
function filterParams(stateId: number, filter: SectionFilter): HttpParams {
  let params = new HttpParams().set('stateId', stateId);
  for (const [key, value] of Object.entries(filter)) {
    if (value !== null && value !== undefined && value !== '') {
      params = params.set(key, String(value));
    }
  }
  return params;
}

/**
 * El catálogo territorial, contra `msvc-geography`.
 *
 * `/catalog` es la API de administración (sólo ADMIN). Las localidades siguen
 * en sus endpoints de siempre, `/localitiesSection`.
 */
@Injectable({ providedIn: 'root' })
export class TerritoryService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_geography;
  }

  getSections(
    stateId: number,
    filter: SectionFilter,
    page: number,
    size: number
  ): Observable<CatalogPage<CatalogSection>> {
    const params = filterParams(stateId, filter)
      .set('page', page)
      .set('size', size);
    return this.http.get<CatalogPage<CatalogSection>>(
      `${this.base}/catalog/sections`,
      { params }
    );
  }

  /** El polígono de la sección en GeoJSON (404 si no tiene geometría). */
  getSectionGeometry(sectionId: number): Observable<Geometry> {
    return this.http.get<Geometry>(
      `${this.base}/catalog/sections/${sectionId}/geometry`
    );
  }

  /** Los totales de las tarjetas, con el mismo filtro que la lista. */
  getSectionsSummary(
    stateId: number,
    filter: SectionFilter
  ): Observable<CatalogSummary> {
    return this.http.get<CatalogSummary>(
      `${this.base}/catalog/sections/summary`,
      { params: filterParams(stateId, filter) }
    );
  }

  updateSection(
    id: number,
    change: SectionChange
  ): Observable<CatalogSection> {
    return this.http.patch<CatalogSection>(
      `${this.base}/catalog/sections/${id}`,
      change
    );
  }

  getMunicipalities(stateId: number): Observable<CatalogMunicipality[]> {
    return this.http.get<CatalogMunicipality[]>(
      `${this.base}/catalog/municipalities`,
      { params: { stateId } }
    );
  }

  renameMunicipality(id: number, name: string): Observable<void> {
    return this.http.patch<void>(`${this.base}/catalog/municipalities/${id}`, {
      name,
    });
  }

  getFederalDistricts(stateId: number): Observable<CatalogDistrict[]> {
    return this.http.get<CatalogDistrict[]>(
      `${this.base}/catalog/federal-districts`,
      { params: { stateId } }
    );
  }

  getLocalDistricts(stateId: number): Observable<CatalogDistrict[]> {
    return this.http.get<CatalogDistrict[]>(
      `${this.base}/catalog/local-districts`,
      { params: { stateId } }
    );
  }

  /** Las localidades vigentes de la sección; las de baja lógica se quedan fuera. */
  getLocalities(sectionId: number): Observable<SectionLocality[]> {
    return this.http
      .get<{ LocalitiesSections: SectionLocality[] }>(
        `${this.base}/localitiesSection/section/${sectionId}`
      )
      .pipe(
        map((body) =>
          (body?.LocalitiesSections ?? [])
            .filter((l) => !l.deleted)
            .sort((a, b) =>
              (a.localidad ?? '').localeCompare(b.localidad ?? '')
            )
        )
      );
  }

  createLocality(
    section: CatalogSection,
    name: string
  ): Observable<SectionLocality> {
    return this.http.post<SectionLocality>(
      `${this.base}/localitiesSection/register`,
      {
        municipio: section.municipalityName,
        seccion: section.section,
        localidad: name,
        seccionId: section.id,
      }
    );
  }

  renameLocality(
    locality: SectionLocality,
    name: string
  ): Observable<SectionLocality> {
    return this.http.put<SectionLocality>(
      `${this.base}/localitiesSection/editLocation/${locality.id}`,
      { ...locality, localidad: name }
    );
  }

  deleteLocality(id: number): Observable<void> {
    return this.http.delete<void>(
      `${this.base}/localitiesSection/deleteLocation/${id}`
    );
  }
}
