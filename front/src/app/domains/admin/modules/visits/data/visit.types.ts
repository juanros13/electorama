/** Sección asignada a un usuario (catálogo de step1). */
export interface SectionByUser {
  seccionGeoId: number;
  seccionNumber: number;
  seccionId?: number;
  deleted: string | null;
}

/** Persona visitada que abrió la puerta (payload completo). */
export interface VisitedPersonOpened {
  names: string | null;
  lastName: string | null;
  surName: string | null;
  phone: string | null;
  ageOfThePersonWhoOpensTheDoor: number | string;
  sexOfTheOneWhoOpensTheDoor: string;
  isRelated: boolean;
  openTheDoor: boolean;
  street: string;
  externalNumber: string;
  longitude: string;
  latitude: string;
  visitNumber: number;
}

/** Persona visitada que no abrió la puerta (solo domicilio). */
export interface VisitedPersonClosed {
  openTheDoor: boolean;
  street: string;
  externalNumber: string;
  longitude: string;
  latitude: string;
  visitNumber: number;
}

export type VisitedPersonPayload = VisitedPersonOpened | VisitedPersonClosed;

/** Alta de visita. */
export interface NewVisit {
  municipalityId: number;
  seccionId: number;
  coordinateZoneId: string;
  localityId: number;
  visitDate: string;
  numberOfVisited: number;
  userCreatedId: string;
  visitedPeople: VisitedPersonPayload[];
  referenceAuthId: string;
}

export interface Visit {
  id: number;
  municipalityId: number;
  seccionId: number;
  coordinateZoneId: string;
  localityId: number;
  visitDate: string;
  numberOfVisited: number | null;
  created: string;
  updated: string;
  deleted: string | null;
  userCreatedId: string;
  visitedPeople: any[];
}

/** Detalle de una persona visitada (modo edición). */
export interface PersonVisitedDetail {
  id: number;
  names: string;
  lastName: string;
  surName: string;
  adultsInHome: number;
  phone: string;
  deliveredGazette: boolean;
  notes: string;
  visitNumber: number;
  deleted: string | null;
  visitedEntity: Visit;
}
