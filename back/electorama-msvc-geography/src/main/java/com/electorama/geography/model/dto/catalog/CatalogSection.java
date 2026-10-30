package com.electorama.geography.model.dto.catalog;

import java.math.BigDecimal;

/**
 * Una sección tal como la ve la pantalla de catálogos.
 *
 * <p>{@code section} va como texto con su padding ("0249") porque así la escribe el INE; el {@code
 * Integer} de {@code SeccionEntity} es deuda que no se arrastra a la API nueva.
 */
public record CatalogSection(
    Long id,
    String section,
    Integer stateId,
    Long municipalityId,
    String municipalityName,
    Long federalDistrictId,
    Integer federalDistrict,
    Long localDistrictId,
    Integer localDistrict,
    Integer type,
    Integer meta,
    Boolean status,
    Boolean hasGeometry,
    Long localities,
    /** LN 2027; null en las secciones que la fuente no traía (G5). */
    Integer nominalList,
    /** % de la LN que es meta. Con LN, la meta es {@code round(LN × % / 100)}. */
    BigDecimal goalPercentage) {}
