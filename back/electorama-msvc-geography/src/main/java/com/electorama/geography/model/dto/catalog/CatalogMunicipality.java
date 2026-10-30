package com.electorama.geography.model.dto.catalog;

/** Un municipio con el agregado de sus secciones vigentes. */
public record CatalogMunicipality(
    Long id,
    String code,
    String name,
    Integer stateId,
    Long sections,
    Long meta,
    Boolean hasGeometry,
    /** Suma de la LN de sus secciones vigentes (G5). */
    Long nominalList,
    /**
     * Votación de MORENA 2024, total municipal de la campaña (G6); referencia, no cómputo oficial.
     */
    Integer morenaVote2024) {}
