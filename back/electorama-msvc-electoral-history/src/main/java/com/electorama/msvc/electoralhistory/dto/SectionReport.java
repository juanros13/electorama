package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/**
 * Las secciones de un territorio (municipio o distrito) en una elección.
 *
 * @param territory el total del territorio, igual que en {@link TerritoryReport}
 * @param sections una por sección, con {@code code} = número de sección
 */
public record SectionReport(
    ElectionSummary election,
    String groupBy,
    TerritoryLevel level,
    TerritoryResult territory,
    List<TerritoryResult> sections) {}
