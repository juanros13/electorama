package com.electorama.msvc.users.model.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Una cuenta para quien se nombra en un puesto de la estructura.
 *
 * @param email también es el usuario con el que entra
 * @param role rol del realm que le toca por su nivel (CORDINATOR_MUNICIPAL, CORDINATOR_ZONE,
 *     PROMOTER o sus _ALT)
 */
public record AccountRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, message = "la contraseña lleva al menos 8 caracteres") String password,
    @NotBlank String firstName,
    @NotBlank String lastName,
    @NotBlank String role) {

  @Override
  public String toString() {
    // la contraseña nunca va a los logs
    return "AccountRequest[email=" + email + ", role=" + role + "]";
  }
}
