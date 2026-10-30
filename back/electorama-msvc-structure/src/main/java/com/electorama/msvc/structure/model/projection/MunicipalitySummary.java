package com.electorama.msvc.structure.model.projection;

/**
 * La estructura principal de un municipio: cuánta gente tiene y cuántos convencidos lleva. Es la
 * parte "real" del tablero de campaña; lo requerido (meta, LN) sale de geography.
 */
public interface MunicipalitySummary {

  Integer getMunicipalityGeoId();

  /** Coordinadores de zona: el "promotor" del Excel de la campaña. */
  Long getPromoters();

  /** Promotores: el "movilizador" del Excel de la campaña. */
  Long getMobilizers();

  /** Suma de las metas de los coordinadores de zona. */
  Long getGoal();

  /** Convencidos vigentes y activos. */
  Long getMembers();
}
