package com.electorama.msvc.users.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Cada error con su código: 403 sin permiso, 404 no existe, 409 duplicado, 502 Keycloak caído. El
 * modelo viejo contestaba 404 por falta de rol y 400 por todo lo demás.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> handleApi(ApiException ex) {
    if (ex.getStatus().is5xxServerError()) {
      log.error("{}: {}", ex.getCode(), ex.getMessage(), ex.getCause());
    }
    return ResponseEntity.status(ex.getStatus())
        .body(
            ErrorResponse.builder()
                .code(ex.getCode())
                .message(ex.getMessage())
                .detail(ex.getDetail())
                .build());
  }

  @ExceptionHandler(AccessDeniedException.class)
  ResponseEntity<ErrorResponse> handleForbidden(AccessDeniedException ex) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(ErrorResponse.builder().code("FORBIDDEN").message("Sin permiso").build());
  }

  @ExceptionHandler(IllegalArgumentException.class)
  ResponseEntity<ErrorResponse> handleBadRequest(IllegalArgumentException ex) {
    return ResponseEntity.badRequest()
        .body(ErrorResponse.builder().code("BAD_REQUEST").message(ex.getMessage()).build());
  }

  /** Un cuerpo que no pasa {@code @Valid}: 400 con el primer campo que falla. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> handleInvalid(MethodArgumentNotValidException ex) {
    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(f -> f.getField() + ": " + f.getDefaultMessage())
            .orElse("Datos inválidos");
    return ResponseEntity.badRequest()
        .body(ErrorResponse.builder().code("BAD_REQUEST").message(message).build());
  }

  // Cualquier otra cosa: 500 con mensaje genérico; la causa real va al log, no al cliente.
  @ExceptionHandler(Exception.class)
  ResponseEntity<ErrorResponse> handleException(Exception ex) {
    log.error("Error no manejado en la API de usuarios", ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(
            ErrorResponse.builder()
                .code("INTERNAL_ERROR")
                .message("Ocurrió un error inesperado.")
                .build());
  }
}
