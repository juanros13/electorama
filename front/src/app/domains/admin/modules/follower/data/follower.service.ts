import { AppConfigService } from '@/app/core/config/app-config';
import { ActionType, ModuleType } from '@/app/core/data/enum/ActionType';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { IpService } from '@/app/core/ip/ip.service';
import { LogfileService } from '@/app/core/logfile/logfile.service';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import { FollowerPromoter } from './follower.types';

@Injectable({ providedIn: 'root' })
export class FollowerService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private ip = inject(IpService);
  private logService = inject(LogfileService);

  private get base(): string {
    return this.appConfig.config.url_followers;
  }

  getFollowers(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string,
    idPromoter: string | null,
    idCoordinator: string | null
  ): Observable<any> {
    // NOTA: el backend movió el listado paginado a /followers/v2.0 (la raíz /followers
    // solo acepta POST para crear → un GET ahí devuelve 405).
    let url = `${this.base}/followers/v2.0?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (search) {
      url += `&search=${search}`;
    }
    if (idCoordinator && idPromoter) {
      url += `&coordinatorAuthId=${idCoordinator}&promoterAuthId=${idPromoter}`;
    } else if (idCoordinator) {
      url += `&coordinatorAuthId=${idCoordinator}`;
    } else if (idPromoter) {
      url += `&promoterAuthId=${idPromoter}`;
    }
    return this.http.get<any>(url).pipe(map((response) => response.followers));
  }

  getFollowersAll(): Observable<any> {
    return this.http.get<any>(`${this.base}/followers/listAll`);
  }

  getFollowersByCoordinatorid(authId: string): Observable<any> {
    return this.http.get<any>(
      `${this.base}/followers/getFollowerByCoordinatorId/${authId}`
    );
  }

  getFollowerById(idFollower: string): Observable<any> {
    return this.http.get<any>(`${this.base}/followers/${idFollower}`);
  }

  deleteFollower(idFollower: number): Observable<any> {
    return this.http.delete<any>(`${this.base}/followers/${idFollower}`).pipe(
      tap(() =>
        this.logAction(
          ActionType.DELETE_FOLLOWER,
          ModuleType.FOLLOWERS,
          `Se eliminó el simpatizante con id ${idFollower}`,
          null
        )
      )
    );
  }

  createFollower(follower: any): Observable<any> {
    return this.http.post<any>(`${this.base}/followers/createNew`, follower).pipe(
      tap(() =>
        this.logAction(
          ActionType.CREATE_FOLLOWER,
          ModuleType.FOLLOWERS,
          `Se creó el simpatizante ${follower.names} ${follower.lastName} ${follower.surName}`,
          JSON.stringify(follower)
        )
      )
    );
  }

  createCitizenFollower(follower: any): Observable<any> {
    return this.http.post<any>(`${this.base}/followers`, follower).pipe(
      tap(() =>
        this.logAction(
          ActionType.CREATE_CITIZEN_ISSUES,
          ModuleType.CITIZEN_ISSUES,
          `Beneficiario creado desde citizen-issues: ${follower.names} ${follower.lastName} ${follower.surName}`,
          JSON.stringify(follower)
        )
      )
    );
  }

  convertToPromoter(follower: FollowerPromoter): Observable<any> {
    return this.http.put<any>(`${this.base}/followers/${follower.id}`, follower).pipe(
      tap(() =>
        this.logAction(
          ActionType.CONVERT_PROMOTER,
          ModuleType.FOLLOWERS,
          `Se convirtió en movilizador el simpatizante ${follower.names} ${follower.lastName} ${follower.surName}`,
          JSON.stringify(follower)
        )
      )
    );
  }

  updateFollower(follower: any, idFollower: any): Observable<any> {
    return this.http.put<any>(`${this.base}/followers/${idFollower}`, follower).pipe(
      tap(() =>
        this.logAction(
          ActionType.UPDATE_FOLLOWER,
          ModuleType.FOLLOWERS,
          `Se actualizó el simpatizante ${follower.names} ${follower.lastName} ${follower.surName}`,
          JSON.stringify(follower)
        )
      )
    );
  }

  getCatalogSex(): Observable<any> {
    return this.http.get<any>(`${this.base}/catSex`);
  }

  getMaritalStatus(): Observable<any> {
    return this.http.get<any>(`${this.base}/catMaritalStatus`);
  }

  getFamilyRole(): Observable<any> {
    return this.http.get<any>(`${this.base}/catFamilyRole`);
  }

  getReportFollowerByCoordinator(): Observable<Blob> {
    return this.http.post(
      `${this.base}/reportFollowers/downloadFollowersByCordinator`,
      null,
      { responseType: 'blob' }
    );
  }

  /** Auditoría: IP best-effort + log (reemplaza la duplicación del viejo). */
  private logAction(
    action: ActionType,
    module: ModuleType,
    message: string,
    data: string | null
  ): void {
    const log: Log = {
      action,
      quantity: 1,
      message,
      data,
      accionId: null,
      module,
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
