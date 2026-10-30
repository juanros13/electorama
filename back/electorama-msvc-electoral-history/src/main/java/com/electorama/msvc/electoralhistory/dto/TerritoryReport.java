package com.electorama.msvc.electoralhistory.dto;

import java.util.List;

/**
 * Una elección repartida por municipio, distrito local o distrito federal.
 *
 * @param state el total estatal: incluye lo que no es de ningún territorio del nivel (voto
 *     anticipado, en prisión y en el extranjero), que por eso no aparece en {@code territories}
 * @param candidatesAvailable si la fuente trae los nombres de las personas candidatas
 */
public record TerritoryReport(
    ElectionSummary election,
    String groupBy,
    TerritoryLevel level,
    boolean candidatesAvailable,
    TerritoryResult state,
    List<TerritoryResult> territories) {}
