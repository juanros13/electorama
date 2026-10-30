package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/**
 * Un cargo en todos sus años (2018, 2021, 2024 los que haya), por municipio o distrito.
 *
 * @param elections en orden cronológico, con su fuente
 */
public record TrendReport(
    String office,
    String groupBy,
    TerritoryLevel level,
    List<ElectionSummary> elections,
    TrendRow state,
    List<TrendRow> territories) {}
