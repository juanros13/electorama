package com.electorama.msvc.structure.model.projection;

/**
 * Un coordinador de zona de la estructura principal con lo que rindió: sus promotores y los
 * convencidos que cuelgan de él (de sus promotores y propios).
 */
public interface SupporterCoordinatorRow {

  Long getPositionId();

  String getCode();

  /** SECTION o MUNICIPALITY (coordinación municipal). */
  String getTerritoryType();

  Integer getSectionGeoId();

  Integer getMunicipalityGeoId();

  /** El último titular del puesto; null si nunca lo tuvo. */
  String getHolderName();

  Integer getGoal();

  Long getPromoters();

  Long getMembers();

  Long getVoted();
}
