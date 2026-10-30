package com.electorama.msvc.structure.model.enums;

/** Niveles del organigrama, de arriba hacia abajo. */
public enum Level {
  REFERENT,
  ZONE_COORDINATOR,
  PROMOTER;

  /** Menor es más alto. Se usa para validar que un puesto cuelgue de uno superior. */
  public int orden() {
    return ordinal() + 1;
  }
}
