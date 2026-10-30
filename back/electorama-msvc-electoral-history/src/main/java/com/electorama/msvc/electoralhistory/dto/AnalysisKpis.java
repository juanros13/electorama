package com.electorama.msvc.electoralhistory.dto;

/**
 * Los números de arriba del análisis.
 *
 * @param brand4tPct el bloque 4T en la elección de la marca (gubernatura o presidencia)
 * @param mayor4tPct el bloque 4T en ayuntamientos
 * @param abstention lista nominal menos votos en ayuntamientos
 */
public record AnalysisKpis(
    double brand4tPct,
    double mayor4tPct,
    double brandGapPp,
    int mayoralties,
    int mayoraltiesLost,
    int mayoraltiesClose,
    int localDistricts,
    int localDistrictsLost,
    int sections,
    int sectionsLost,
    int sectionsCompetitive,
    long abstention,
    Double turnout) {}
