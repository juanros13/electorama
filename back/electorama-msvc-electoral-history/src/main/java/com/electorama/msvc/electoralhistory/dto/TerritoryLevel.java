package com.electorama.msvc.electoralhistory.dto;

/**
 * Cómo se reparte el estado en un reporte. Cada casilla trae los tres (E3), así que cualquier
 * elección se puede leer en cualquiera, sin importar en qué territorio se compitió.
 */
public enum TerritoryLevel {
  MUNICIPALITY("municipality_cve", null),
  LOCAL_DISTRICT("local_district", "DISTRITO LOCAL"),
  FEDERAL_DISTRICT("federal_district", "DISTRITO FEDERAL");

  /** La columna de los agregados de E5; fija, nunca viene del cliente. */
  private final String column;

  /** Cómo se nombra un distrito; los municipios usan su nombre. */
  private final String prefix;

  TerritoryLevel(String column, String prefix) {
    this.column = column;
    this.prefix = prefix;
  }

  public String column() {
    return column;
  }

  public String prefix() {
    return prefix;
  }

  /** El scope de la elección cuyo territorio coincide con este nivel. */
  public boolean matchesScope(String scope) {
    return name().equals(scope);
  }
}
