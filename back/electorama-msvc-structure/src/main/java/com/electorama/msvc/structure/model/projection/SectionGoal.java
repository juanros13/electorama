package com.electorama.msvc.structure.model.projection;

/**
 * La meta repartida entre los coordinadores de zona de una sección, cuántos coordinadores son y
 * cuántos promotores tiene la sección, en la estructura principal.
 */
public interface SectionGoal {

  Integer getSectionGeoId();

  Long getGoal();

  /** Coordinadores de zona (el "promotor" del Excel de la campaña). */
  Long getPositions();

  /** Promotores (el "movilizador" del Excel de la campaña). */
  Long getPromoters();
}
