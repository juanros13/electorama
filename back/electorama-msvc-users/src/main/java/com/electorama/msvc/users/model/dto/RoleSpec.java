package com.electorama.msvc.users.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Un rol de estructura que msvc-structure necesita para un nivel de campaña. {@code copyMenusFrom}
 * es el rol heredado del que toma sus menús si el rol es nuevo.
 */
public record RoleSpec(
    @NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]{0,79}$") String role,
    @Min(1) int seniority,
    Long campaignId,
    String description,
    String copyMenusFrom) {}
