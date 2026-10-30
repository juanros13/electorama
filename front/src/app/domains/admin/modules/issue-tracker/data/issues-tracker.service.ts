import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';
import { ListIncidentsContent, SubCategory } from './issues-track.types';

@Injectable({ providedIn: 'root' })
export class IssuesTrackerService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_geo_elections;
  }

  getIssuesTrack(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    municipalityId: string,
    categoryId: string
  ): Observable<any> {
    let url = `${this.base}/pollingBoothIssues?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (municipalityId) {
      url += `&municipalityId=${municipalityId}`;
    }
    if (categoryId) {
      url += `&subCategoryId=${categoryId}`;
    }
    return this.http.get<any>(url).pipe(map((res) => res.list));
  }

  getListTotalIncidentsStats(
    municipalityId: string,
    categoryId: string
  ): Observable<ListIncidentsContent[]> {
    let url = `${this.base}/pollingBoothIssues/listTotalIncidents?1=1`;
    if (municipalityId) {
      url += `&municipalityId=${municipalityId}`;
    }
    if (categoryId) {
      url += `&subCategoryId=${categoryId}`;
    }
    return this.http
      .get<any>(url)
      .pipe(map((res) => res.listTotalIncidents ?? []));
  }

  getCategoriesIssues(): Observable<SubCategory[]> {
    return this.http
      .get<any>(`${this.base}/pollingBoothIssues/getCategory`)
      .pipe(
        map((res) => res.getIssuesByPollingBooth?.[0]?.subCategories ?? [])
      );
  }
}
