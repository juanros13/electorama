package com.electorama.msvc.electoralhistory.dto;

/**
 * Un municipio (ayuntamiento) o distrito local (diputación) visto desde MORENA y sus aliados.
 *
 * @param winner quien ganó, con la persona si la fuente la trae
 * @param best4t la mejor candidatura del bloque 4T: MORENA (sola o en coalición) o un aliado que
 *     compitió aparte
 * @param rival la mejor candidatura fuera del bloque 4T
 * @param marginPp best4t − rival, en pp sobre la votación total; negativo si se perdió
 * @param bloc4tPct el bloque 4T del año (MORENA y aliados, juntos o separados)
 * @param brand4tPct el bloque 4T en la elección de referencia de la «marca» (gubernatura o
 *     presidencia)
 * @param brandGapPp brand4tPct − bloc4tPct: cuánto se quedó el candidato local debajo de la marca
 * @param change4tPp el bloque 4T contra la elección anterior del mismo cargo
 * @param alliesSplit se perdió, pero el bloque 4T sumado superaba al ganador: los aliados
 *     compitieron separados
 * @param allyWinner ganó un aliado compitiendo contra MORENA (el PT o el PVEM solos)
 * @param allyRunnerUp el segundo lugar fue otra candidatura del bloque 4T: los aliados compitieron
 *     entre sí
 */
public record TerritoryRisk(
    int code,
    String name,
    RiskStatus status,
    GroupVotes winner,
    GroupVotes best4t,
    GroupVotes rival,
    Double marginPp,
    Double bloc4tPct,
    Double brand4tPct,
    Double brandGapPp,
    Double previous4tPct,
    Double change4tPp,
    Double turnout,
    long nominalList,
    boolean alliesSplit,
    boolean allyWinner,
    boolean allyRunnerUp) {}
