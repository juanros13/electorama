import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  CatIssueCategory,
  CatIssueSolutionDocumentsCategory,
  CatIssueStatus,
  CatIssueTypes,
  IssueDAO,
  IssueDTO,
  IssueDetailDAO,
  Provider,
  Resource,
  ResultIssueStatus,
  SolutionDTO,
} from './citizen-issues.types';

@Injectable({ providedIn: 'root' })
export class CitizenIssuesService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_citizen_issues;
  }

  // ---------------------------------------------------------------------------
  // Catálogos
  // ---------------------------------------------------------------------------

  getCatIssuesTypes(): Observable<CatIssueTypes[]> {
    return this.http.get<CatIssueTypes[]>(`${this.base}/catIssueTypes`);
  }

  getCatIssuesStatus(): Observable<CatIssueStatus> {
    return this.http.get<CatIssueStatus>(`${this.base}/catIssueStatus`);
  }

  getCatIssuesCategory(): Observable<CatIssueCategory> {
    return this.http.get<CatIssueCategory>(`${this.base}/catIssueCategory`);
  }

  getCatIssueSolutionDocumentsCategory(): Observable<CatIssueSolutionDocumentsCategory> {
    return this.http.get<CatIssueSolutionDocumentsCategory>(
      `${this.base}/catIssueSolutionDocumentsCategory`
    );
  }

  getCitizenIssuer(): Observable<any> {
    return this.http.get<any>(
      `${this.appConfig.config.url_user}/v1.0.0/getCitizenIssuer`
    );
  }

  // ---------------------------------------------------------------------------
  // Incidencias (issues / details)
  // ---------------------------------------------------------------------------

  createIssue(issue: IssueDTO): Observable<IssueDAO> {
    return this.http.post<IssueDAO>(`${this.base}/issue`, issue);
  }

  updateIssue(issue: IssueDTO): Observable<IssueDAO> {
    return this.http.put<IssueDAO>(`${this.base}/issue`, issue);
  }

  getIssues(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<any> {
    let url = `${this.base}/issue/findAllIssues?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<any>(url);
  }

  getIssuesDetails(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string,
    invoice: string,
    municipalityId: number = 0,
    userCreateId: string = '',
    categoryId: number = 0,
    statusId: number = 0,
    urgent: number | boolean = 0
  ): Observable<any> {
    let url = `${this.base}/issue/findAllIssueDetails?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}&municipalityId=${municipalityId}&userCreateId=${userCreateId}&categoryId=${categoryId}&statusId=${statusId}&urgent=${urgent}`;
    if (search) {
      url += `&search=${search}`;
    }
    if (invoice) {
      url += `&invoice=${invoice}`;
    }
    return this.http.get<any>(url);
  }

  getIssueDetailById(id: string): Observable<IssueDetailDAO> {
    return this.http.get<IssueDetailDAO>(`${this.base}/issue/findDetailById/${id}`);
  }

  updateIssueDetail(
    detail: IssueDetailDAO,
    id: string
  ): Observable<IssueDetailDAO> {
    return this.http.put<IssueDetailDAO>(
      `${this.base}/issue/updateIssueDetail/${id}`,
      detail
    );
  }

  getResultsIssuesStatus(
    municipalityId: number = 0,
    userCreateId: string = '',
    categoryId: number = 0,
    statusId: number = 0,
    urgent: number | boolean = 0,
    search: string = '',
    invoice: string = ''
  ): Observable<ResultIssueStatus> {
    let url = `${this.base}/issue/getReportIssuesStatus?municipalityId=${municipalityId}&userCreateId=${userCreateId}&categoryId=${categoryId}&statusId=${statusId}&urgent=${urgent}`;
    if (search) {
      url += `&search=${search}`;
    }
    if (invoice) {
      url += `&invoice=${invoice}`;
    }
    return this.http.get<ResultIssueStatus>(url);
  }

  deleteIssueDetail(id: number): Observable<IssueDetailDAO> {
    return this.http.delete<IssueDetailDAO>(
      `${this.base}/issue/deleteIssueDetailById/${id}`
    );
  }

  deleteIssueDetail4Ever(id: number): Observable<IssueDetailDAO> {
    return this.http.delete<IssueDetailDAO>(
      `${this.base}/issue/hardDeleteIssueDetailById/${id}`
    );
  }

  getIssueById(id: string): Observable<IssueDAO> {
    return this.http.get<IssueDAO>(`${this.base}/issue/findIssueById/${id}`);
  }

  deleteIssue(id: number): Observable<IssueDAO> {
    return this.http.delete<IssueDAO>(`${this.base}/issue/deleteIssueById/${id}`);
  }

  // ---------------------------------------------------------------------------
  // Recursos y proveedores
  // ---------------------------------------------------------------------------

  getAllResources(): Observable<Resource[]> {
    return this.http.get<Resource[]>(`${this.base}/resources`);
  }

  saveResource(resource: Resource): Observable<Resource> {
    return this.http.post<Resource>(`${this.base}/resources`, resource);
  }

  getResoursesByCategoryId(idCategory: string): Observable<Resource[]> {
    return this.http.get<Resource[]>(
      `${this.base}/resources/getResoursesByCategoryId/${idCategory}`
    );
  }

  getAllProviders(): Observable<Provider[]> {
    return this.http.get<Provider[]>(`${this.base}/provider`);
  }

  saveProvider(name: string): Observable<Provider> {
    return this.http.post<Provider>(`${this.base}/provider`, { name });
  }

  // ---------------------------------------------------------------------------
  // Soluciones y archivos
  // ---------------------------------------------------------------------------

  saveSolution(solution: SolutionDTO): Observable<any> {
    return this.http.post<any>(`${this.base}/solution`, solution);
  }

  saveIssueFile(fileObj: FormData): Observable<any> {
    return this.http.post<any>(`${this.base}/dropbox/upload`, fileObj);
  }

  downloadFile(pathFile: string): Observable<Blob> {
    return this.http.get(`${this.base}/dropboxDownload?path=${pathFile}`, {
      responseType: 'blob',
    });
  }

  loadFile(pathFile: string): Observable<Blob> {
    return this.http.get(`${this.base}/dropboxDownload?path=${pathFile}`, {
      responseType: 'blob',
    });
  }

  // ---------------------------------------------------------------------------
  // Reportes
  // ---------------------------------------------------------------------------

  downloadIssuesByTimeToCSV(dates: any): Observable<Blob> {
    return this.http.post(`${this.base}/issue/downloadIssuesByTime`, { ...dates }, {
      responseType: 'blob',
    });
  }

  getDataCitiIssuesDash(params: any): Observable<any> {
    return this.http.post<any>(`${this.base}/reports/getIssuesStatus`, {
      ...params,
    });
  }
}
