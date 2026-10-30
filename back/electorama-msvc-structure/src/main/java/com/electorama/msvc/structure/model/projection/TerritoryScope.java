package com.electorama.msvc.structure.model.projection;

/**
 * Una sección que un coordinador cubre, y desde qué puesto la cubre.
 *
 * <p>{@code fromMunicipality} distingue las dos formas de cubrir: un puesto sobre la sección, o un
 * puesto sobre el municipio que la contiene. Es lo que antes eran las filas de {@code
 * users_seccions}, con la diferencia de que el ámbito municipal se calcula contra el catálogo en
 * vez de estar copiado.
 */
public interface TerritoryScope {

  Long getTerritoryId();

  String getTerritoryCode();

  Integer getSectionGeoId();

  Integer getMunicipalityGeoId();

  Long getPositionId();

  Integer getGoal();

  Integer getChildLimit();

  Boolean getFromMunicipality();
}
