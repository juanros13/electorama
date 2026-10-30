import { AppConfigService } from '@/app/core/config/app-config';
import { ActionType, ModuleType } from '@/app/core/data/enum/ActionType';
import { Locality, SectionInfo } from '@/app/core/data/model/GeographyTypes';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { IpService } from '@/app/core/ip/ip.service';
import { LogfileService } from '@/app/core/logfile/logfile.service';
import { UserCoordinatorZone } from '@/app/core/user/user.types';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { NewVisit, PersonVisitedDetail, SectionByUser, Visit } from './visit.types';

@Injectable({ providedIn: 'root' })
export class VisitService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private ip = inject(IpService);
  private logService = inject(LogfileService);

  private get visits(): string {
    return this.appConfig.config.url_visits;
  }
  private get geography(): string {
    return this.appConfig.config.url_geography;
  }
  private get user(): string {
    return this.appConfig.config.url_user;
  }
  private get ows(): string {
    return this.appConfig.config.url_geoserver_ows;
  }

  // --- catálogos step1 ---
  getMunicipalitiesByStateId(stateId: string): Observable<any> {
    return this.http.get<any>(`${this.geography}/municipality/entity/${stateId}`);
  }

  getSectionsByUser(authId: string): Observable<SectionByUser[]> {
    return this.http
      .get<any>(`${this.user}/v1.0.0/userSeccion/${authId}`)
      .pipe(map((res) => res?.seccions ?? res ?? []));
  }

  getUsersByMunicipalityCoordinator(): Observable<UserCoordinatorZone[]> {
    return this.http.get<UserCoordinatorZone[]>(`${this.user}/v1.0.0/readUsers`);
  }

  getSectionInfoById(sectionId: string): Observable<SectionInfo> {
    return this.http.get<SectionInfo>(`${this.geography}/seccion/${sectionId}`);
  }

  getLocalitiesBySectionId(sectionId: string): Observable<Locality[]> {
    return this.http
      .get<any>(`${this.geography}/localitiesSection/section/${sectionId}`)
      .pipe(map((res) => res?.LocalitiesSections ?? []));
  }

  // --- listado / detalle ---
  getVisits(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<any> {
    let url = `${this.visits}/visitedPeople?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<any>(url);
  }

  getVisitById(id: string): Observable<PersonVisitedDetail> {
    return this.http.get<PersonVisitedDetail>(`${this.visits}/visitedPeople/${id}`);
  }

  /** Suma una visita más a una persona ya visitada. */
  addOneVisit(id: string): Observable<any> {
    return this.http.put<any>(`${this.visits}/visitedPeople/addVisit/${id}`, null).pipe(
      tap(() =>
        this.logAction(
          ActionType.ADD_ONE_VISIT,
          ModuleType.VISITED,
          `Se agregó una visita a la persona ${id}`,
          String(id)
        )
      )
    );
  }

  /** Reporte de visitas en CSV por rango de fechas. */
  downloadReportCsv(dates: { startDate: string; endDate: string }): Observable<Blob> {
    return this.http.post(`${this.visits}/reportVisited/reportVisitedAllToCsv`, dates, {
      responseType: 'blob',
    });
  }

  // --- escritura ---
  addNewVisit(visit: NewVisit): Observable<any> {
    return this.http.post<any>(`${this.visits}/visited`, visit).pipe(
      tap(() =>
        this.logAction(
          ActionType.INSERT_VISIT,
          ModuleType.VISITED,
          `Se registró una visita con ${visit.visitedPeople.length} persona(s)`,
          JSON.stringify(visit)
        )
      )
    );
  }

  updateVisit(visit: Visit): Observable<any> {
    return this.http.put<any>(`${this.visits}/visited/${visit.id}`, visit).pipe(
      tap(() =>
        this.logAction(
          ActionType.UPDATE_VISIT,
          ModuleType.VISITED,
          `Se actualizó la visita con id ${visit.id}`,
          JSON.stringify(visit)
        )
      )
    );
  }

  deletePersonVisited(id: string): Observable<any> {
    return this.http.delete<any>(`${this.visits}/visited/visitedPeople/${id}`).pipe(
      tap(() =>
        this.logAction(
          ActionType.DELETE_VISITED_PEOPLE,
          ModuleType.VISITED,
          `Se eliminó la persona visitada con id ${id}`,
          String(id)
        )
      )
    );
  }

  // --- capas GeoServer (WFS GeoJSON) ---
  getMunicipalitiesGeoJson(): Observable<any> {
    const url =
      `${this.ows}?service=WFS&CQL_FILTER=cve_ent=27&version=1.0.0` +
      `&request=GetFeature&typeName=serveros%3Ageo_municipios_tab&outputFormat=application%2Fjson`;
    return this.http.get<any>(url);
  }

  getSectionGeoJson(seccionGeoId: number): Observable<any> {
    const url =
      `${this.ows}?service=WFS&version=1.0.0&CQL_FILTER=id=${seccionGeoId}` +
      `&request=GetFeature&typeName=serveros%3Ageo_secciones&outputFormat=application%2Fjson`;
    return this.http.get<any>(url);
  }

  /** Auditoría best-effort: IP + log. */
  private logAction(
    action: ActionType,
    module: ModuleType,
    message: string,
    data: string | null
  ): void {
    const log: Log = { action, quantity: 1, message, data, accionId: null, module, ip: '' };
    this.ip.getIPAddress().subscribe({
      next: (res: any) => {
        log.ip = res?.ip ?? '';
        this.logService.insertNewLog(log).subscribe();
      },
      error: () => this.logService.insertNewLog(log).subscribe(),
    });
  }
}
