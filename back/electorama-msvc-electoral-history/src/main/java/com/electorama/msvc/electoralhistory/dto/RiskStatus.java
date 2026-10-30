package com.electorama.msvc.electoralhistory.dto;

/**
 * Qué tan en riesgo está un territorio para MORENA y sus aliados, por el margen entre su
 * candidatura y la mejor rival (puntos porcentuales sobre la votación total).
 */
public enum RiskStatus {
  /** La candidatura de MORENA no ganó. */
  LOST,
  /** Ganó por menos de 5 pp. */
  CLOSE,
  /** Ganó por menos de 10 pp. */
  COMPETITIVE,
  /** Ganó por 10 pp o más. */
  SAFE;

  public static final double CLOSE_PP = 5;
  public static final double COMPETITIVE_PP = 10;

  /**
   * @param marginPp la candidatura de MORENA menos la mejor rival; negativo si perdió
   */
  public static RiskStatus of(Double marginPp) {
    if (marginPp == null || marginPp < 0) {
      return LOST;
    }
    if (marginPp < CLOSE_PP) {
      return CLOSE;
    }
    return marginPp < COMPETITIVE_PP ? COMPETITIVE : SAFE;
  }
}
