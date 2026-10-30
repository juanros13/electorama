/** Sección asignada a un usuario (selección al crear coordinador). */
export interface UserSection {
  id: number;
  meta: number;
  municipalityId: number;
  seccionGeoId: number;
  seccionNumber: string;
  userAuthId: string;
  deleted: string | null;
}

export interface UserInsert {
  password: string;
  email: string;
  firstName: string;
  lastName: string;
}

export interface SectionsInsert {
  seccionGeoId: number;
  seccionNumber: number;
  municipioId: number;
}

export interface InsertCoordinatorZone {
  user: UserInsert;
  sections: SectionsInsert[];
}
