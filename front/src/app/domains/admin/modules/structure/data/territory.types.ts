/**
 * El catálogo territorial de `msvc-geography` (`/catalog`).
 *
 * Es el marco del INE: secciones, municipios y distritos. Casi todo es de
 * sólo lectura porque lo define el INE y se actualiza con los scripts G* del
 * microservicio; lo editable es la meta de la sección —el techo— y su
 * vigencia, el nombre del municipio y las localidades.
 */

export interface CatalogSection {
  id: number;
  /** Con su padding, como lo publica el INE: "0249". */
  section: string;
  stateId: number;
  municipalityId: number | null;
  municipalityName: string | null;
  federalDistrictId: number | null;
  federalDistrict: number | null;
  localDistrictId: number | null;
  localDistrict: number | null;
  /** Tipo de sección del INE: 1 urbana, 2 mixta, 3 rural. */
  type: number | null;
  /**
   * El techo de la sección: `round(nominalList × goalPercentage / 100)` cuando
   * hay LN. La meta de cada coordinador vive en su puesto.
   */
  meta: number | null;
  /** Lista nominal 2027. `null` en las secciones que la fuente no traía. */
  nominalList: number | null;
  /** % de la LN que es meta (61 por default). */
  goalPercentage: number | null;
  status: boolean | null;
  hasGeometry: boolean;
  localities: number;
}

export interface CatalogMunicipality {
  id: number;
  code: string;
  name: string;
  stateId: number;
  sections: number;
  meta: number;
  hasGeometry: boolean;
  /** Suma de la LN de sus secciones vigentes. */
  nominalList: number;
  /**
   * Votación de MORENA 2024, total municipal del Excel de la campaña.
   * Referencia, no cómputo oficial; `null` si no hay dato.
   */
  morenaVote2024: number | null;
}

export interface CatalogDistrict {
  id: number;
  number: number;
  stateId: number;
  /** Sólo los federales pueden salir en `false`: los que la distritación 2023 quitó. */
  status: boolean;
  sections: number;
  meta: number;
  municipalities: string | null;
  hasGeometry: boolean;
}

export interface CatalogPage<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
}

/** Totales de las secciones que pasan el filtro. */
export interface CatalogSummary {
  sections: number;
  municipalities: number;
  federalDistricts: number;
  localDistricts: number;
  /** Suma de los techos de meta. */
  meta: number;
  /** Suma de la lista nominal 2027. */
  nominalList: number;
  /** Secciones que todavía no tienen LN (su techo no se calcula). */
  withoutNominalList: number;
}

/**
 * Lo que se cambia de una sección; lo que no viaja no se toca. Con LN, la meta
 * no se manda: la calcula el back con el porcentaje.
 */
export interface SectionChange {
  meta?: number | null;
  nominalList?: number | null;
  goalPercentage?: number | null;
  status?: boolean | null;
}

export interface SectionFilter {
  municipalityId?: number | null;
  federalDistrict?: number | null;
  localDistrict?: number | null;
  /** `null` trae vigentes y dadas de baja. */
  status?: boolean | null;
  q?: string | null;
}

/** Una localidad de la sección (`geo_localidades_juntos`). */
export interface SectionLocality {
  id: number;
  municipio: string | null;
  seccion: string | null;
  localidad: string | null;
  seccionId: number;
  /** Baja lógica: el endpoint devuelve también las borradas. */
  deleted: string | null;
}

/**
 * La estructura principal completa en una sección (msvc-structure,
 * `/positions/section-overview`): lo mismo que {@link SectionGoal} más los
 * convencidos y los nombres de los coordinadores de zona.
 */
export interface SectionOverview {
  sectionGeoId: number;
  goal: number;
  positions: number;
  promoters: number;
  /** Convencidos vigentes y activos anclados a puestos de la sección. */
  members: number;
  /** Titulares de las coordinaciones de zona, separados por coma. */
  coordinators: string | null;
}

/** La meta repartida entre los coordinadores de una sección (msvc-structure). */
export interface SectionGoal {
  sectionGeoId: number;
  goal: number;
  /** Coordinadores de zona: el "promotor" del Excel de la campaña. */
  positions: number;
  /** Promotores: el "movilizador" del Excel de la campaña. */
  promoters: number;
}
