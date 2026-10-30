package com.electorama.msvc.structure.exception;

import java.time.OffsetDateTime;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Las reglas duras del modelo viven en la base —el EXCLUDE de vigencias, la jerarquía de puestos,
 * el linaje inmutable— y llegan aquí como {@link DataIntegrityViolationException}. Se traducen a
 * 409 con el mensaje del trigger, que explica el porqué mejor que cualquier texto que se pueda
 * inventar aquí.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(StructureException.class)
  public ResponseEntity<Map<String, Object>> handle(StructureException e) {
    Map<String, Object> body = new java.util.LinkedHashMap<>(body(e.getStatus(), e.getMessage()));
    if (e.getDetail() != null) {
      body.put("detail", e.getDetail());
    }
    return ResponseEntity.status(e.getStatus()).body(body);
  }

  /**
   * Un {@code RAISE EXCEPTION} de un trigger (SQLSTATE P0001, como la jerarquía de puestos) no es
   * una violación de integridad para Spring y llegaba como 500. Es una regla del modelo: 409 con su
   * mensaje.
   */
  @ExceptionHandler({UncategorizedSQLException.class, JpaSystemException.class})
  public ResponseEntity<Map<String, Object>> handleTrigger(RuntimeException e) {
    String message = rootMessage(e);
    log.warn("la base rechazó la operación: {}", message);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, message));
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException e) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(body(HttpStatus.FORBIDDEN, "sin permiso para esta operación"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handle(DataIntegrityViolationException e) {
    String message = rootMessage(e);
    log.warn("la base rechazó la operación: {}", message);
    return ResponseEntity.status(HttpStatus.CONFLICT).body(body(HttpStatus.CONFLICT, message));
  }

  private Map<String, Object> body(HttpStatus status, String message) {
    return Map.of(
        "timestamp", OffsetDateTime.now().toString(),
        "status", status.value(),
        "error", status.getReasonPhrase(),
        "message", message == null ? "" : message);
  }

  private String rootMessage(Throwable e) {
    Throwable current = e;
    while (current.getCause() != null) {
      current = current.getCause();
    }
    return clean(current.getMessage());
  }

  /**
   * El mensaje de Postgres trae "ERROR: " al inicio y el contexto del trigger ("Where: función…")
   * al final; al cliente sólo le sirve la regla.
   */
  static String clean(String message) {
    if (message == null) {
      return null;
    }
    String m = message.replaceFirst("^ERROR:\\s*", "");
    int where = m.indexOf("\n  Where:");
    return (where >= 0 ? m.substring(0, where) : m).trim();
  }
}
