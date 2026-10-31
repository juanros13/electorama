package com.electorama.msvc.structure.model.projection;

/**
 * Un puesto con su titular vigente y sus dos conteos, tal como sale de la consulta nativa.
 *
 * <p>Es una proyección y no una entidad porque la vigencia de la asignación es un {@code TSTZRANGE}
 * que JPA no mapea, y porque lo que la operación necesita ver de un puesto —quién lo ocupa, cuántos
 * hijos tiene, cuántos convencidos lleva— no vive en una sola tabla.
 */
public interface PositionSummary {

  Long getId();

  /** La función heredada del nivel (REFERENT, ZONE_COORDINATOR, PROMOTER) o null. */
  String getLevel();

  /** El nivel de la campaña (V3): su id, su rango (1 = cúspide) y su nombre. */
  Long getLevelId();

  Integer getLevelRank();

  String getLevelName();

  String getCode();

  /** La clave legible por sección (V7): S-0233A1 el promotor, S-0233A1-01 el movilizador. */
  String getSectionKey();

  Integer getGoal();

  Integer getChildLimit();

  Long getParentId();

  Long getStructureId();

  Long getTerritoryId();

  String getTerritoryType();

  String getTerritoryCode();

  Integer getSectionGeoId();

  Integer getMunicipalityGeoId();

  String getHolderAuthId();

  Long getHolderPersonId();

  String getHolderName();

  Long getChildCount();

  Long getMembershipCount();
}
