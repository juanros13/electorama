package com.electorama.msvc.documentmanagement.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDashFollowerException extends RuntimeException {
  private String code;
  private String message;

  public SimpleDashFollowerException(String message) {
    super(message);
  }
}
