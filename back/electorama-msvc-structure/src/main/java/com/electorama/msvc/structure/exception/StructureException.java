package com.electorama.msvc.structure.exception;

import org.springframework.http.HttpStatus;

/** Un error de negocio del modelo de estructura, con el código HTTP que le corresponde. */
public class StructureException extends RuntimeException {

  private final HttpStatus status;

  /** Dato extra para el cliente, p.ej. el authId de una cuenta que ya existe. */
  private final transient Object detail;

  public StructureException(HttpStatus status, String message) {
    this(status, message, null);
  }

  public StructureException(HttpStatus status, String message, Object detail) {
    super(message);
    this.status = status;
    this.detail = detail;
  }

  public static StructureException notFound(String what, Object id) {
    return new StructureException(HttpStatus.NOT_FOUND, what + " " + id + " no existe");
  }

  public static StructureException conflict(String message) {
    return new StructureException(HttpStatus.CONFLICT, message);
  }

  public static StructureException badRequest(String message) {
    return new StructureException(HttpStatus.BAD_REQUEST, message);
  }

  public HttpStatus getStatus() {
    return status;
  }

  public Object getDetail() {
    return detail;
  }
}
