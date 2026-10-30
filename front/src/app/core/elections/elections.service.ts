import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

/**
 * Datos electorales (casillas, reportes). Servicio transversal compartido por
 * defense-vote y por el módulo elections (Oleada B); se irá ampliando.
 */
@Injectable({ providedIn: 'root' })
export class ElectionsService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_geo_elections;
  }

  getMunicipalitiesReport(): Observable<any> {
    return this.http.get<any>(`${this.base}/pollingBooth/getReportMunicipalities`);
  }

  getAllPollingBooth(): Observable<any> {
    return this.http.get<any>(`${this.base}/pollingBooth/findAllPollingBoothImproved`);
  }

  getAllPollingBoothFederal(): Observable<any> {
    return this.http.get<any>(
      `${this.base}/pollingBooth/getPollingsBoothsByFederalDistrict`
    );
  }

  getAllPollingBoothLocal(): Observable<any> {
    return this.http.get<any>(
      `${this.base}/pollingBooth/getPollingsBoothsByLocalDistrict`
    );
  }

  // --- Catálogos electorales (antes en MapsNacionalService) ---

  getElectionType(): Observable<any> {
    return this.http.get<any>(`${this.base}/electionType/`);
  }

  getElectionPosition(): Observable<any> {
    return this.http.get<any>(`${this.base}/electionPosition/`);
  }

  // --- Resultados ---

  getPoliticalSheetReview(electionId: string, typeId: string): Observable<any> {
    let url = `${this.base}/election/electionResultSheets/`;
    if (electionId) {
      url += electionId;
    }
    if (typeId) {
      url += `/${typeId}`;
    }
    return this.http.get<any>(url);
  }

  getPoliticalPartyResult(electionId: string, typeId: string): Observable<any> {
    let url = `${this.base}/election/electionResult/`;
    if (electionId) {
      url += electionId;
    }
    if (typeId) {
      url += `/${typeId}`;
    }
    return this.http.get<any>(url);
  }

  /** Foto de candidato (blob). Usa el dominio de followers (dropbox). */
  getPhotoBase64(path: string): Observable<Blob> {
    return this.http.get(
      `${this.appConfig.config.url_followers}/dropboxDownload?path=${path}`,
      { responseType: 'blob' }
    );
  }

  // --- Validación de actas (casillas) ---

  /** Lista paginada de casillas para validación de actas. */
  getSheetList(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    filters: SheetListFilters
  ): Observable<any> {
    const url =
      `${this.base}/pollingBooth/getPollingBooths?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}` +
      this.sheetFilterQuery(filters);
    return this.http.get<any>(url).pipe(map((res) => res.getPollingBooths));
  }

  /** Totales de avance de validación (digital/física/total). */
  getSheetListAdvance(
    page: number,
    pageSize: number,
    filters: SheetListFilters
  ): Observable<any> {
    const url =
      `${this.base}/pollingBooth/getValideSheet?pageNo=${page}&pageSize=${pageSize}` +
      this.sheetFilterQuery(filters);
    return this.http.get<any>(url).pipe(map((res) => res.getValideSheet));
  }

  /** Resultado de una asociación de elección/casilla (la respuesta es un array). */
  getElectionResultByIdA(electionAssociationId: string): Observable<any[]> {
    return this.http.post<any[]>(
      `${this.base}/results/electionsResultsSheets/findElectionsAssociation`,
      { electionAssociationIds: [electionAssociationId] }
    );
  }

  getElectionResultById(electionAssociationId: string): Observable<any[]> {
    return this.http.post<any[]>(
      `${this.base}/results/electionResult/findElectionsAssociationB`,
      { electionAssociationIds: [electionAssociationId] }
    );
  }

  updateResultSheet(results: ElectionResultUpdateRequest[]): Observable<any> {
    return this.http.post<any>(
      `${this.base}/results/electionsResultsSheets/updateElectionsResultsSheets`,
      results
    );
  }

  updateValidate(
    electionAssociationId: string,
    validateId: string
  ): Observable<any> {
    return this.http.post<any>(
      `${this.base}/pollingBoothElectionAssociation/updateValidate/${electionAssociationId}/${validateId}`,
      null
    );
  }

  // --- Flujo de votación ---

  getVotingFlow(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    municipalityId: string
  ): Observable<any> {
    let url = `${this.base}/votingFlow/votingFlow?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (municipalityId) {
      url += `&municipalityId=${municipalityId}`;
    }
    return this.http.get<any>(url).pipe(map((res) => res.getVotingFlow));
  }

  getVotingFlowProgress(): Observable<any> {
    return this.http.get<any>(`${this.base}/votingFlow/getProgressVote`);
  }

  // --- Apertura de casillas (estatus) ---

  getPollingBoothStatusList(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    municipalityId: string,
    statusId: string
  ): Observable<any> {
    let url = `${this.base}/pollingBooth/getPollingBoothsStatus?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (statusId) {
      url += `&statusId=${statusId}`;
    }
    if (municipalityId) {
      url += `&municipalityId=${municipalityId}`;
    }
    return this.http.get<any>(url).pipe(map((res) => res.getPollingBooths));
  }

  /** Marcaje de votantes por nivel (1=municipio, 2=federal, 3=local). */
  getDataMarkVoting(id: number): Observable<any> {
    return this.http
      .get<any>(
        `${this.appConfig.config.url_followers}/followersMark/getFollowerMarkVoters/${id}`
      )
      .pipe(map((res) => res.getFollowerMarkVoters));
  }

  getPollingBoothCount(
    municipalityId: string,
    statusId: string
  ): Observable<any> {
    let url = `${this.base}/pollingBooth/getCountPollingsBooths?pageNo=0&pageSize=10`;
    if (statusId) {
      url += `&statusId=${statusId}`;
    }
    if (municipalityId) {
      url += `&municipalityId=${municipalityId}`;
    }
    return this.http.get<any>(url).pipe(map((res) => res.getCountPollingsBooths));
  }

  private sheetFilterQuery(f: SheetListFilters): string {
    let q = '';
    if (f.federalDistrictId) q += `&federalDistrictId=${f.federalDistrictId}`;
    if (f.localDistrictId) q += `&localDistrictId=${f.localDistrictId}`;
    if (f.sectionId) q += `&sectionNum=${f.sectionId}`;
    if (f.typeId) q += `&statusValidateId=${f.typeId}`;
    if (f.electionId) q += `&electionPositionId=${f.electionId}`;
    if (f.municipalityId) q += `&municipalityId=${f.municipalityId}`;
    return q;
  }
}

export interface SheetListFilters {
  municipalityId: string;
  federalDistrictId: string;
  localDistrictId: string;
  sectionId: string;
  typeId: string;
  electionId: string;
}

export interface ElectionResultUpdateRequest {
  politicalPartyId: number;
  electionAssociationId: number;
  result: number;
}
