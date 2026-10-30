package com.electorama.msvc.electoralhistory.dto;

/**
 * Una sección que vale la pena vigilar en el ayuntamiento: se perdió, se ganó por menos de 10 pp o
 * el candidato local se quedó muy debajo de la marca.
 *
 * @param marginVotes votos de la mejor candidatura del bloque 4T menos los de la mejor rival fuera
 *     de él; negativo = los que faltaron para ganar la sección
 */
public record SectionRisk(
    int section,
    int municipality,
    String municipalityName,
    Integer localDistrict,
    Integer federalDistrict,
    long nominalList,
    Double turnout,
    RiskStatus status,
    String best4tLabel,
    double best4tPct,
    String rivalLabel,
    double rivalPct,
    double marginPp,
    long marginVotes,
    Double bloc4tPct,
    Double brand4tPct,
    Double brandGapPp) {}
