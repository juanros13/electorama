import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { SafeHouse } from './safe-house.types';

@Injectable({ providedIn: 'root' })
export class SafeHouseService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_geography;
  }

  getSafeHouses(): Observable<SafeHouse[]> {
    return this.http.get<SafeHouse[]>(`${this.base}/safeHouse/getByMunicipality`);
  }

  deleteSafeHouse(house: SafeHouse): Observable<any> {
    return this.http.delete<any>(`${this.base}/safeHouse/${house.id}`);
  }
}
