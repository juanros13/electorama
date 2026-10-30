package com.electorama.msvc.electoralhistory.dto;

import java.util.Map;

/**
 * Una sección de una elección, con lo necesario para cruzarla con otra fuente (la estructura de
 * campaña): LN, votos, participación y los votos por bloque del año.
 *
 * @param turnout votos de casillas ordinarias / su LN; null si no hay LN
 * @param blocVotes votos por bloque del año (MORENA_ALLIES, PAN_PRI_PRD, MC, OTHERS)
 */
public record SectionTotals(
    int section,
    int municipality,
    Integer localDistrict,
    Integer federalDistrict,
    long nominalList,
    long totalVotes,
    Double turnout,
    Map<String, Long> blocVotes) {}
