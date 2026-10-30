package com.electorama.msvc.electoralhistory.exception;

import org.springframework.http.HttpStatus;

/** Un error de consulta del histórico electoral, con el código HTTP que le corresponde. */
public class ElectoralHistoryException extends RuntimeException {

  private final HttpStatus status;

  public ElectoralHistoryException(HttpStatus status, String message) {
    super(message);
    this.status = status;
  }

  public static ElectoralHistoryException notFound(String what, Object id) {
    return new ElectoralHistoryException(HttpStatus.NOT_FOUND, what + " " + id + " no existe");
  }

  public static ElectoralHistoryException badRequest(String message) {
    return new ElectoralHistoryException(HttpStatus.BAD_REQUEST, message);
  }

  public HttpStatus getStatus() {
    return status;
  }
}
