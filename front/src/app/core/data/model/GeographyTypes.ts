// Subset portado para Fase 2. Los tipos que dependen de leaflet (p.ej. MapArray)
// se portarán junto al módulo de mapas en la Oleada C.

export interface Municipality {
    id: number;
    nomMun: string;
    entidadId: number;
}

export interface MunicipalitySelect {
    cve_ent: string;
    cve_mun: string;
    cve_munc: string;
    entidad_id: number;
    id: number;
    nom_ent: string;
    nom_mun: string;
}

export interface Locality {
    id: number;
    localidad: string;
    municipio: string;
    seccion: number;
    seccionId: number;
    deleted: string;
}

export interface Section {
    id: number;
    seccion: number;
    distritoLocalId: number;
    distritoFederalId: number;
    entidadId: number;
}

export interface SectionInfo {
    id: number;
    distritoFederalId: number;
    distritoLocalId: number;
    entidadId: number;
    seccion: number;
    municipioId: number;
    nomMun: string;
    nomEnt: string;
    meta: string;
    deleted?: Date;
}

export interface Coordinator {
    id: number;
    nombre: string;
}
