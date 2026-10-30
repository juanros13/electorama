package com.electorama.msvc.structure.model.projection;

/**
 * La estructura principal en una sección, completa: meta repartida, gente y avance. Es la parte
 * "real" del análisis de secciones a vigilar; lo requerido (meta de la sección, LN) sale de
 * geography y el resultado electoral, de electoral-history.
 */
public interface SectionOverview {

  Integer getSectionGeoId();

  /** Suma de las metas de los coordinadores de zona de la sección. */
  Long getGoal();

  /** Coordinadores de zona (el "promotor" del Excel de la campaña). */
  Long getPositions();

  /** Promotores (el "movilizador" del Excel de la campaña). */
  Long getPromoters();

  /** Convencidos vigentes y activos anclados a puestos de la sección. */
  Long getMembers();

  /** Los titulares de las coordinaciones de zona, separados por coma; null si no hay. */
  String getCoordinators();
}
