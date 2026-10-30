import { AppConfigService } from '@/app/core/config/app-config';
import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { map, Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CanvasWallsService {
  private http = inject(HttpClient);
  private appConfig = inject(AppConfigService);

  private get base(): string {
    return this.appConfig.config.url_canvas_anda_walls;
  }

  getCanvasWalls(
    sort: string,
    order: string,
    page: number,
    pageSize: number,
    search: string
  ): Observable<any> {
    let url = `${this.base}/canvasAndWalls?sortBy=${sort}&orderBy=${order}&pageNo=${page}&pageSize=${pageSize}`;
    if (search) {
      url += `&search=${search}`;
    }
    return this.http.get<any>(url).pipe(map((res) => res.canvasAndWall));
  }

  deleteCanvasWalls(id: number): Observable<any> {
    return this.http.delete<any>(`${this.base}/canvasAndWalls/${id}`);
  }

  /** Foto de evidencia (blob). Usa el dominio de followers (dropbox). */
  getPhoto(path: string): Observable<Blob> {
    return this.http.get(
      `${this.appConfig.config.url_followers}/dropboxDownload?path=${path}`,
      { responseType: 'blob' }
    );
  }
}
