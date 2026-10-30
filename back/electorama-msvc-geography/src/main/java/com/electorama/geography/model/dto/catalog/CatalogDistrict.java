package com.electorama.geography.model.dto.catalog;

/**
 * Un distrito federal o local con el agregado de sus secciones vigentes.
 *
 * <p>{@code status} sólo existe en los federales: los que desaparecieron en la distritación 2023
 * quedan con {@code false} en vez de borrarse, porque su id es FK en otros servicios. Los locales
 * siempre salen vigentes.
 */
public record CatalogDistrict(
    Long id,
    Integer number,
    Integer stateId,
    Boolean status,
    Long sections,
    Long meta,
    String municipalities,
    Boolean hasGeometry) {}
