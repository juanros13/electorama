package com.electorama.msvc.electoralhistory.exception;

import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Los errores de consulta salen con el mismo cuerpo que en structure. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ElectoralHistoryException.class)
  public ResponseEntity<Map<String, Object>> handle(ElectoralHistoryException e) {
    HttpStatus status = e.getStatus();
    return ResponseEntity.status(status)
        .body(
            Map.of(
                "timestamp", OffsetDateTime.now().toString(),
                "status", status.value(),
                "error", status.getReasonPhrase(),
                "message", e.getMessage() == null ? "" : e.getMessage()));
  }
}
