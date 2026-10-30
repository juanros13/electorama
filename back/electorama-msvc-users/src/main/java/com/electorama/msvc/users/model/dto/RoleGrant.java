package com.electorama.msvc.users.model.dto;

import jakarta.validation.constraints.NotBlank;

/** Un rol de estructura que se le agrega a una cuenta existente (al ligarla a un puesto). */
public record RoleGrant(@NotBlank String role) {}
