package com.electorama.geography.model.dto.catalog;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * El % de meta de una campaña, para todas las secciones de su estado o, si viene {@code
 * municipalityId}, de ese municipio. La meta de cada sección con LN se recalcula (G5); las que no
 * tienen LN sólo guardan el porcentaje.
 */
public record GoalUpdateRequest(
    @NotNull Integer stateId,
    Long municipalityId,
    @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal goalPercentage) {}
