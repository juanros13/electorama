package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/**
 * El análisis de un año para dirección: dónde está el riesgo en lo que se vuelve a elegir
 * (ayuntamientos y diputaciones locales), con la marca (gubernatura o presidencia) de referencia.
 *
 * @param findings frases armadas con los datos, de la más a la menos importante
 * @param sections secciones a vigilar del ayuntamiento, de mayor a menor riesgo
 */
public record AnalysisReport(
    int year,
    ElectionSummary mayor,
    ElectionSummary localDeputy,
    ElectionSummary brand,
    Integer previousYear,
    List<String> findings,
    AnalysisKpis kpis,
    List<TerritoryRisk> municipalities,
    List<TerritoryRisk> localDistricts,
    List<RivalTrend> rivals,
    List<SectionRisk> sections) {}
