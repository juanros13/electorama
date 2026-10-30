/** Sección asignada a un usuario (para selección en el alta de promotor). */
export interface SectionByUserPromoter {
  id: number;
  meta: number;
  municipalityId: number;
  seccionGeoId: number;
  seccionNumber: string;
  userAuthId: string;
  deleted: string | null;
}

/** DTO para crear/actualizar un promotor (incluye alta de cuenta de usuario). */
export interface PromoterDTO {
  id?: number;
  authId?: string | null;
  promoterAuthId: string | null;
  userCreateId: string;
  municipalityId: number;
  seccionId: number;
  followerSirenaId: number | null;
  names: string;
  lastName: string;
  surName: string;
  curp: string;
  ine: string;
  vencimiento: number;
  cellPhoneNumber: string;
  street: string;
  interiorNumber: string;
  outdoorNumber: string;
  neighborhoodId: string;
  postalCode: string;
  notes: string;
  email: string;
  user: {
    firstName: string;
    lastName: string;
    email: string;
    password: string | null;
    identification: string;
  };
}

/** Fila de la lista de promotores (subset backend-driven de PromoterDAO). */
export interface Promoter {
  id: number;
  promoterAuthId?: string;
  names?: string;
  lastName?: string;
  surName?: string;
  curp?: string;
  email?: string;
  cellPhoneNumber?: string;
  neighborhoodId?: string | number;
  deleted?: string | null;
  followersCount?: number;
  followersGoal?: number;
}
