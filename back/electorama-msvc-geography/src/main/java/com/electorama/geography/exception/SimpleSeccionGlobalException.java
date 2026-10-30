package com.electorama.geography.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleSeccionGlobalException extends RuntimeException {

  private String code;
  private String message;

  public SimpleSeccionGlobalException(String message) {
    super(message);
  }
}
