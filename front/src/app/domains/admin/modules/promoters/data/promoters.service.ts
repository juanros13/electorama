import { AppConfigService } from '@/app/core/config/app-config';
import { ActionType, ModuleType } from '@/app/core/data/enum/ActionType';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { IpService } from '@/app/core/ip/ip.service';
import { LogfileService } from '@/app/core/logfile/logfile.service';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import {
  Promoter,
  PromoterDTO,
  SectionByUserPromoter,
} from './promoters.types';

@Injectable({ providedIn: 'root' })
export class PromotersService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private ip = inject(IpService);
  private logService = inject(LogfileService);

  private get base(): string {
    return this.appConfig.config.url_followers;
  }

  getPromoters(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<any> {
    let url = `${this.base}/promoters?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<any>(url).pipe(map((response) => response.list));
  }

  /** Promotores de una sección (por seccionGeoId). */
  getAllPromotores(idSection: number): Observable<any> {
    return this.http.get<any>(
      `${this.base}/promoters/listPromotersBySectionId/${idSection}`
    );
  }

  getAllTotalPromoters(idCoordinador: string | null = null): Observable<any> {
    const url = idCoordinador
      ? `${this.base}/promoters/listPromoters/${idCoordinador}`
      : `${this.base}/promoters/listPromoters`;
    return this.http.get<any>(url);
  }

  getPromoterById(idPromoter: string): Observable<Promoter> {
    return this.http.get<Promoter>(`${this.base}/promoters/${idPromoter}`);
  }

  /** Secciones asignadas a un usuario (para el alta de promotor). */
  getSectionsByUser(authId: string): Observable<SectionByUserPromoter[]> {
    return this.http.get<SectionByUserPromoter[]>(
      `${this.appConfig.config.url_user}/v1.0.0/userSeccion/${authId}`
    );
  }

  createPromoter(promoter: PromoterDTO): Observable<any> {
    return this.http.post<any>(`${this.base}/promoters`, promoter).pipe(
      tap(() =>
        this.logAction(
          ActionType.CREATE_PROMOTER,
          `Se creó el movilizador ${promoter.names} ${promoter.lastName} ${promoter.surName}`,
          JSON.stringify(promoter)
        )
      )
    );
  }

  updatePromoter(promoter: PromoterDTO, id: string): Observable<any> {
    return this.http.put<any>(`${this.base}/promoters/${id}`, promoter).pipe(
      tap(() =>
        this.logAction(
          ActionType.UPDATE_PROMOTER,
          `Se actualizó el movilizador ${promoter.names} ${promoter.lastName} ${promoter.surName}`,
          JSON.stringify(promoter)
        )
      )
    );
  }

  deletePromoter(promoter: Promoter): Observable<any> {
    return this.http.delete<any>(`${this.base}/promoters/${promoter.id}`).pipe(
      tap(() =>
        this.logAction(
          ActionType.DELETE_PROMOTER,
          `Se eliminó el movilizador ${promoter.names} ${promoter.lastName} ${promoter.surName}`,
          null
        )
      )
    );
  }

  private logAction(
    action: ActionType,
    message: string,
    data: string | null
  ): void {
    const log: Log = {
      action,
      quantity: 1,
      message,
      data,
      accionId: null,
      module: ModuleType.PROMOTERS,
      ip: '',
    };
    this.ip.getIPAddress().subscribe({
      next: (res: any) => {
        log.ip = res?.ip ?? '';
        this.logService.insertNewLog(log).subscribe();
      },
      error: () => this.logService.insertNewLog(log).subscribe(),
    });
  }
}
