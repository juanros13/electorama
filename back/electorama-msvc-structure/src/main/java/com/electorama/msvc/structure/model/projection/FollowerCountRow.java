package com.electorama.msvc.structure.model.projection;

/**
 * Una fila de reporte: cuántos convencidos y cuántos promotores hay bajo una clave de agrupación.
 *
 * <p>Las cuatro claves van en la misma proyección y las que no aplican salen nulas, porque los tres
 * reportes del modelo viejo devolvían subconjuntos de estas mismas columnas: por municipio, por
 * coordinador de zona (con su sección y su referente) y por referente.
 */
public interface FollowerCountRow {

  Long getMunicipalityId();

  Long getSectionId();

  String getCoordinatorZoneAuthId();

  String getReferentAuthId();

  /** Convencidos vigentes en el rango de fechas. */
  Long getFollowers();

  /** Puestos de promotor vigentes bajo esa clave. */
  Long getPromoters();

  /** Suma de las metas de esos puestos. */
  Long getGoal();
}
