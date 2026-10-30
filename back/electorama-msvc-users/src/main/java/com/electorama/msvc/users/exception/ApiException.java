package com.electorama.msvc.users.exception;

import org.springframework.http.HttpStatus;

/**
 * Un error de la API con el código HTTP que le corresponde.
 *
 * <p>Sustituye a la familia vieja ({@code SimpleBankingGlobalException} y derivadas) que respondía
 * 400 a todo —duplicados, no encontrados y caídas de Keycloak por igual—, así que el cliente no
 * podía distinguir un correo repetido de un servidor caído.
 */
public class ApiException extends RuntimeException {

  private final HttpStatus status;
  private final String code;
  /** Dato extra para el cliente, p.ej. el authId de la cuenta que ya existe. */
  private final transient Object detail;

  public ApiException(HttpStatus status, String code, String message, Object detail) {
    super(message);
    this.status = status;
    this.code = code;
    this.detail = detail;
  }

  public static ApiException notFound(String message) {
    return new ApiException(HttpStatus.NOT_FOUND, "NOT_FOUND", message, null);
  }

  public static ApiException conflict(String code, String message, Object detail) {
    return new ApiException(HttpStatus.CONFLICT, code, message, detail);
  }

  public static ApiException badRequest(String message) {
    return new ApiException(HttpStatus.BAD_REQUEST, "BAD_REQUEST", message, null);
  }

  /** Tiene sesión, pero la cuenta que pide no es de su rama (o su rol no alcanza). */
  public static ApiException forbidden(String message) {
    return new ApiException(HttpStatus.FORBIDDEN, "FORBIDDEN", message, null);
  }

  /** msvc-structure no respondió: no se puede saber si la cuenta es de su rama. */
  public static ApiException structure(String message) {
    return new ApiException(HttpStatus.BAD_GATEWAY, "STRUCTURE", message, null);
  }

  /** Keycloak no respondió o respondió algo inesperado: el problema es del otro lado. */
  public static ApiException keycloak(String message) {
    return new ApiException(HttpStatus.BAD_GATEWAY, "KEYCLOAK", message, null);
  }

  public HttpStatus getStatus() {
    return status;
  }

  public String getCode() {
    return code;
  }

  public Object getDetail() {
    return detail;
  }
}
