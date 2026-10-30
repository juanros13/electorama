import { AppConfigService } from '@/app/core/config/app-config';
import { ActionType, ModuleType } from '@/app/core/data/enum/ActionType';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { IpService } from '@/app/core/ip/ip.service';
import { LogfileService } from '@/app/core/logfile/logfile.service';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable, tap } from 'rxjs';
import {
  FollowerMobilization,
  GetMobilizationAlt,
  GetMobilizationAltReport,
  MobilizationByFederalDistrict,
  MobilizationByLocalDistrict,
  MobilizationByMunicipality,
} from './mobilization.types';

@Injectable({ providedIn: 'root' })
export class MobilizationService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private ip = inject(IpService);
  private logService = inject(LogfileService);

  private get base(): string {
    return this.appConfig.config.url_followers;
  }

  // --- Reportes de avance ---

  getMobilizationByMunicipality(): Observable<MobilizationByMunicipality[]> {
    return this.http
      .get<any>(`${this.base}/mobilization/getMobilizationByMunicipality`)
      .pipe(map((res) => res.getMobilizationByMunicipality));
  }

  getMobilizationByLocalDistrict(): Observable<MobilizationByLocalDistrict[]> {
    return this.http
      .get<any>(`${this.base}/mobilization/getLocalDistrictMobilization`)
      .pipe(map((res) => res.getLocalDistrictMobilization));
  }

  getMobilizationByFederalDistrict(): Observable<MobilizationByFederalDistrict[]> {
    return this.http
      .get<any>(`${this.base}/mobilization/getFederalDistrictMobilization`)
      .pipe(map((res) => res.getFederalDistrictMobilization));
  }

  getMobilizationReferentReportAlt(): Observable<GetMobilizationAlt[]> {
    return this.http
      .get<GetMobilizationAltReport>(`${this.base}/mobilization/getMobilizationAlt`)
      .pipe(map((res) => res.getMobilizationAlt));
  }

  getMobilizationReferentReport(
    municipalityId: string,
    referentId: string
  ): Observable<any> {
    let url = `${this.base}/mobilization/getMunicipalityByReferentAndCoordinatorMobilization?1=1`;
    if (municipalityId) {
      url += `&municipalityId=${municipalityId}`;
    }
    if (referentId) {
      url += `&referentId=${referentId}`;
    }
    return this.http.get<any>(url);
  }

  // --- Acciones de movilización (usadas por el listado día-D, pendiente) ---

  mobilizeFollower(follower: any, flagMobilize = true): Observable<any> {
    const url = `${this.base}/followers/updateVoteFlag/${follower.id}?voteFlag=${flagMobilize}`;
    return this.http.put<any>(url, follower).pipe(
      tap(() =>
        this.logAction(
          ActionType.MOBILIZE_FOLLOWER,
          1,
          `Se movilizó el simpatizante ${follower.names} ${follower.lastName} ${follower.surName}`,
          JSON.stringify(follower)
        )
      )
    );
  }

  mobilizeFollowerList(followerList: FollowerMobilization): Observable<any> {
    return this.http
      .post<any>(`${this.base}/followers/updateVoteFlagList`, followerList)
      .pipe(
        tap(() =>
          this.logAction(
            ActionType.MOBILIZE_FOLLOWER_LIST,
            followerList.followerIds.length,
            `Se movilizan ${followerList.followerIds.length} simpatizantes`,
            JSON.stringify(followerList)
          )
        )
      );
  }

  private logAction(
    action: ActionType,
    quantity: number,
    message: string,
    data: string | null
  ): void {
    const log: Log = {
      action,
      quantity,
      message,
      data,
      accionId: null,
      module: ModuleType.FOLLOWERS,
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
