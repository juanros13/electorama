import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { PositionLevel } from './organization.types';

/** Un puesto donde quien captura puede dejar convencidos: el suyo o uno de su rama. */
export interface CaptureTarget {
  positionId: number;
  level: PositionLevel;
  /** Clave de sección (S-0233A1-11) o la del territorio. */
  label: string;
  holderName: string | null;
  sectionGeoId: number | null;
  municipalityGeoId: number | null;
  own: boolean;
}

/** Cuántas personas invita (los mismos valores que la app). */
export type PeopleInvited = 'uno' | 'dos' | 'tres' | 'cuatro' | 'cinco' | 'masdecinco';

/** Un convencido nuevo: los mismos datos que pide la app. */
export interface CaptureRequest {
  positionId: number | null;
  supportsMovement: boolean;
  names?: string;
  lastName?: string;
  surName?: string;
  curp?: string;
  ine?: string;
  ineValidity?: number | null;
  birthdate?: string | null;
  sex: 'HOMBRE' | 'MUJER' | null;
  cellPhoneNumber?: string;
  supportWhatsapp?: boolean;
  cellPhoneNumberOther?: string;
  email?: string;
  sectionGeoId: number | null;
  street?: string;
  externalNumber?: string;
  internalNumber?: string;
  neighborhood?: string;
  zipCode?: string;
  committeeParticipation?: boolean;
  pollingRepParticipation?: boolean;
  invitingOthers?: boolean;
  numberOfPeopleInvited?: PeopleInvited | null;
  /** Dónde vive: GPS del dispositivo o el punto marcado en el mapa. */
  latitude?: number | null;
  longitude?: number | null;
}

export interface Captured {
  membershipId: number;
  personId: number;
  positionId: number;
  positionLabel: string;
  /** false si la persona ya estaba en el padrón (por CURP) y sólo se completó. */
  newPerson: boolean;
}

/** Un convencido vigente de un puesto (msvc-structure `/memberships?positionId`). */
export interface SupporterRow {
  id: number;
  personName: string | null;
  curp: string | null;
  cellPhoneNumber: string | null;
  sectionGeoId: number | null;
  source: string;
  validFrom: string;
  latitude: number | null;
  longitude: number | null;
}

/** Un convencido del listado (el puesto y todo lo que cuelga de él). */
export interface SupporterListRow {
  id: number;
  name: string | null;
  curp: string | null;
  phone: string | null;
  /** Clave del territorio de la sección donde vive: "S-0233". */
  section: string | null;
  /** El puesto donde quedó: "S-0233A1-11". */
  positionLabel: string;
  supportsMovement: string | null;
  committee: string | null;
  pollingRep: string | null;
  invitingOthers: string | null;
  captured: string;
  latitude: number | null;
  longitude: number | null;
}

export interface SupporterPage {
  content: SupporterListRow[];
  total: number;
}

export interface SupporterFilter {
  q?: string;
  supports?: 'SI' | 'NO' | null;
  committee?: boolean | null;
  pollingRep?: boolean | null;
  withLocation?: boolean | null;
  /** YYYY-MM-DD, hora de México. */
  from?: string | null;
  to?: string | null;
}

/** Las tarjetas del listado. */
export interface SupporterSummary {
  positionId: number;
  goal: number;
  total: number;
  today: number;
  last7: number;
  supportsYes: number;
  withPhone: number;
  withLocation: number;
  committee: number;
  pollingRep: number;
}

/** Captura de convencidos contra msvc-structure (sin msvc-followers). */
@Injectable({ providedIn: 'root' })
export class CaptureService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return `${this.appConfig.config.url_structure}/memberships`;
  }

  /** Con campaña, sólo los puestos de esa campaña; sin ella, los de todas las activas. */
  getTargets(campaignId?: number): Observable<CaptureTarget[]> {
    const params =
      campaignId == null ? new HttpParams() : new HttpParams().set('campaignId', campaignId);
    return this.http.get<CaptureTarget[]>(`${this.base}/capture-targets`, { params });
  }

  capture(request: CaptureRequest): Observable<Captured> {
    return this.http.post<Captured>(this.base, request);
  }

  /** Buscar y filtrar en el puesto y lo que cuelga de él, paginado. */
  search(positionId: number, filter: SupporterFilter, page: number, size: number): Observable<SupporterPage> {
    let params = new HttpParams().set('positionId', positionId).set('page', page).set('size', size);
    for (const [key, value] of Object.entries(filter)) {
      if (value !== null && value !== undefined && value !== '') {
        params = params.set(key, String(value));
      }
    }
    return this.http.get<SupporterPage>(`${this.base}/search`, { params });
  }

  summary(positionId: number): Observable<SupporterSummary> {
    return this.http.get<SupporterSummary>(`${this.base}/summary`, {
      params: new HttpParams().set('positionId', positionId),
    });
  }

  /** Los convencidos vigentes del puesto, de 100 en 100. */
  getByPosition(positionId: number, page = 0, size = 100): Observable<SupporterRow[]> {
    const params = new HttpParams()
      .set('positionId', positionId)
      .set('page', page)
      .set('size', size);
    return this.http.get<SupporterRow[]>(this.base, { params });
  }
}
