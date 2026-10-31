import { AppConfigService } from '@/app/core/config/app-config';
import { ActionType, ModuleType } from '@/app/core/data/enum/ActionType';
import { Log } from '@/app/core/data/model/LogfileTypes';
import { IpService } from '@/app/core/ip/ip.service';
import { LogfileService } from '@/app/core/logfile/logfile.service';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable, tap } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class FollowerService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);
  private ip = inject(IpService);
  private logService = inject(LogfileService);

  private get base(): string {
    return this.appConfig.config.url_followers;
  }

  getFollowerById(idFollower: string): Observable<any> {
    return this.http.get<any>(`${this.base}/followers/${idFollower}`);
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

  getCatalogSex(): Observable<any> {
    return this.http.get<any>(`${this.base}/catSex`);
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
