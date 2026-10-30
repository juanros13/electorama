package com.electorama.msvc.users.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

/** El cuerpo de un error: código estable, mensaje legible y, si aplica, un dato extra. */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
  private String code;
  private String message;
  private Object detail;
}
