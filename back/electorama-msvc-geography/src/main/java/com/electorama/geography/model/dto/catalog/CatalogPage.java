package com.electorama.geography.model.dto.catalog;

import java.util.List;

/** Una página con su total, contado con los mismos filtros que el contenido. */
public record CatalogPage<T>(List<T> content, int page, int size, long totalElements) {}
