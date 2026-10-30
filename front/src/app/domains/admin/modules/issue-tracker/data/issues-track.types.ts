/** Incidencia de casilla (fila del tablero de incidencias). */
export interface ContentIssuesTrack {
  id: number;
  federalDistrict: number;
  localDistrict: number;
  sectionNumber: number;
  pollingBoothsId: number;
  nameCat: string;
  nameSub: string;
  description: string;
  created: string;
  municipality: string;
}

/** Subcategoría de incidencia (catálogo de filtro). */
export interface SubCategory {
  id: number;
  name: string;
}

/** Total de incidencias por subcategoría (stats). */
export interface ListIncidentsContent {
  id: number;
  subCategoryName: string;
  total: number;
}
