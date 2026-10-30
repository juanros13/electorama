package com.electorama.msvc.users.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** La contraseña nueva que un administrador le pone a una cuenta de la estructura. */
public record PasswordChange(
    @NotBlank @Size(min = 8, message = "la contraseña lleva al menos 8 caracteres")
        String password) {

  @Override
  public String toString() {
    // la contraseña nunca va a los logs
    return "PasswordChange[***]";
  }
}
