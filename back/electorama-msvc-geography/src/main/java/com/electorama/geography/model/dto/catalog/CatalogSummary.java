package com.electorama.geography.model.dto.catalog;

/**
 * Los totales de las secciones que pasan un filtro: cuántas son, en cuántos municipios y distritos
 * caen, la suma de sus techos de meta y de su LN, y cuántas no tienen LN todavía.
 */
public record CatalogSummary(
    long sections,
    long municipalities,
    long federalDistricts,
    long localDistricts,
    long meta,
    long nominalList,
    long withoutNominalList) {}
