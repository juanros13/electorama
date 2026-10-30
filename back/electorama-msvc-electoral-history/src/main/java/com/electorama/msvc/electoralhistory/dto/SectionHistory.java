package com.electorama.msvc.electoralhistory.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Una sección en todas las elecciones cargadas: cómo votó en 2018, 2021 y 2024, cargo por cargo. Es
 * lo que ve quien trabaja la sección (movilizador, promotor) de su propio territorio.
 *
 * @param municipality clave del municipio de la sección (1..17)
 */
public record SectionHistory(int section, Integer municipality, List<Election> elections) {

  /**
   * Una elección en la sección.
   *
   * @param turnout votos de casillas ordinarias / LN; null si no hay LN
   * @param groups de mayor a menor votación
   * @param marginPct ventaja del primero sobre el segundo, en puntos de la votación total
   */
  public record Election(
      int year,
      String office,
      LocalDate electionDate,
      long nominalList,
      long totalVotes,
      Double turnout,
      List<GroupVotes> groups,
      String winner,
      Double marginPct) {}
}
