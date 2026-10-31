package com.electorama.msvc.structure.model.enums;

/**
 * Las tres FUNCIONES heredadas del organigrama (V3). Desde V3 cada campaña define sus propios
 * niveles ({@code campaign_level}); un nivel puede llevar una de estas funciones para conservar la
 * lógica que depende de ella: claves de sección, captura de convencidos, reportes y vistas compat.
 */
public enum Level {
  REFERENT,
  ZONE_COORDINATOR,
  PROMOTER;

  /** Menor es más alto. Ordena las funciones dentro de una campaña. */
  public int orden() {
    return ordinal() + 1;
  }
}
