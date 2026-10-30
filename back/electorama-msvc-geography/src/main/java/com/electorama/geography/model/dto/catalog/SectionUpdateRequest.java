package com.electorama.geography.model.dto.catalog;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.math.BigDecimal;

/**
 * Lo que se puede editar de una sección. Un campo en null no se toca.
 *
 * <p>La meta se calcula con {@code goalPercentage} sobre {@code nominalList} (G5). Sólo las
 * secciones sin LN aceptan {@code meta} directa; en las demás se cambia el porcentaje.
 *
 * <p>Lo demás (número, municipio, distritos) lo define el INE y se actualiza con los scripts G*.
 */
public record SectionUpdateRequest(
    @Min(0) @Max(100000) Integer meta,
    @Min(0) @Max(100000) Integer nominalList,
    @DecimalMin("0") @DecimalMax("100") BigDecimal goalPercentage,
    Boolean status) {}
