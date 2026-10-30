import { AppConfigService } from '@/app/core/config/app-config';
import { UserCoordinatorZone } from '@/app/core/user/user.types';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { InsertCoordinatorZone, UserSection } from './structure.types';

/**
 * Estructura territorial (coordinadores de zona/municipio, secciones).
 * Portado por subset; los métodos de alta/edición (que dependen de
 * StructureTypes) se portan al portar los formularios.
 */
@Injectable({ providedIn: 'root' })
export class StructureService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get userBase(): string {
    return this.appConfig.config.url_user;
  }

  getCoordinatorsZoneByAuthId(): Observable<UserCoordinatorZone[]> {
    return this.http.get<UserCoordinatorZone[]>(
      `${this.userBase}/v1.0.0/getCordinatorZone`
    );
  }

  getCoordinatorsMunicipalitiesByAuthId(): Observable<any> {
    return this.http.get<any>(`${this.userBase}/v1.0.0/getCordinatorMunicipal`);
  }

  getInfoUserByAuthId(authId: string): Observable<any> {
    return this.http.get<any>(`${this.userBase}/v1.0.0/readUser/${authId}`);
  }

  /** Secciones asignadas a un usuario (para asignar al nuevo coordinador). */
  getSectionsByUser(authId: string): Observable<UserSection[]> {
    return this.http.get<UserSection[]>(
      `${this.userBase}/v1.0.0/userSeccion/${authId}`
    );
  }

  /** Alta de coordinador de zona bajo el usuario `authId`. */
  addNewCoordinatorZone(
    coordinator: InsertCoordinatorZone,
    authId: string
  ): Observable<any> {
    return this.http.post<any>(
      `${this.userBase}/v1.0.0/createCordinatorZone/${authId}`,
      coordinator
    );
  }
}
