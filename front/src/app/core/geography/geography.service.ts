import { AppConfigService } from '@/app/core/config/app-config';
import { Municipality } from '@/app/core/data/model/GeographyTypes';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

/**
 * Catálogos y datos geográficos (municipios, localidades, entidades, secciones).
 * Servicio transversal usado por varios módulos. Portado por subset según se
 * necesita; se ampliará al portar más módulos (estructura, mapas, etc.).
 */
@Injectable({ providedIn: 'root' })
export class GeographyService {
    private http = inject(HttpClient);
    private appConfig = inject(AppConfigService);

    private get base(): string {
        return this.appConfig.config.url_geography;
    }

    /** Municipios de una entidad (estado). En el viejo se accedía `.municipios`. */
    getMunicipalitiesByStateId(idEntity: string): Observable<Municipality[]> {
        return this.http
            .get<{ municipios: Municipality[] }>(`${this.base}/municipality/entity/${idEntity}`)
            .pipe(map((response) => response.municipios));
    }

    /** Localidades por nombre de municipio. */
    getLocalitiesByMunicipalityName(municipalityName: string): Observable<any> {
        return this.http.get<any>(
            `${this.base}/v1.0.0/Localities/localitiesByMunicipality/${municipalityName}`
        );
    }

    /** Entidades (estados) de México. */
    getAllEntities(): Observable<any> {
        return this.http.get<any>(`${this.base}/entity/`);
    }

    /** Secciones con su municipio de referencia. */
    getSectionsByMunicipalityReference(): Observable<any> {
        return this.http.get<any>(`${this.base}/seccion/municipalityReference`);
    }

    /** Info de una sección por id. */
    getSectionInfoById(sectionId: string): Observable<any> {
        return this.http.get<any>(`${this.base}/seccion/${sectionId}`);
    }
}
