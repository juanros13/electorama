package com.electorama.msvc.elections.structure.exceptions;

import java.util.Locale;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(SimpleElectionEstructureException.class)
  protected ResponseEntity handleGlobalException(
      SimpleElectionEstructureException simpleElectionEstructureException, Locale locale) {
    return ResponseEntity.badRequest()
        .body(
            ErrorResponse.builder()
                .code(simpleElectionEstructureException.getCode())
                .message(simpleElectionEstructureException.getMessage())
                .build());
  }

  @ExceptionHandler({Exception.class})
  protected ResponseEntity handleException(Exception e, Locale locale) {
    return ResponseEntity.badRequest().body("Exception occur inside API " + e);
  }
}
