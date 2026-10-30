package com.electorama.msvc.visits.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDashVisitException extends RuntimeException {
  private String code;
  private String message;

  public SimpleDashVisitException(String message) {
    super(message);
  }
}
