package com.electorama.msvc.structure.model.projection;

import java.time.Instant;

/** Los convencidos de una campaña en una sección, por dónde viven (su territorio). */
public interface SupporterSectionRow {

  Integer getSectionGeoId();

  /** Estructura principal. */
  Long getMain();

  /** Estructura alterna. */
  Long getAlternate();

  /** Con la marca «votó» (las dos estructuras). */
  Long getVoted();

  Instant getFirstCaptured();

  Instant getLastCaptured();
}
