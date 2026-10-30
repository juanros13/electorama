package com.electorama.geography.model.dto.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** El nombre de un municipio: lo único que cambia sin cambiar el marco (p. ej. Solidaridad). */
public record MunicipalityUpdateRequest(@NotBlank @Size(max = 50) String name) {}
