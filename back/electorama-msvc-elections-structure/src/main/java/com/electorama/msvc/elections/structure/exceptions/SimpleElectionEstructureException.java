package com.electorama.msvc.elections.structure.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleElectionEstructureException extends RuntimeException {

  private String code;
  private String message;

  public SimpleElectionEstructureException(String message) {
    super(message);
  }
}
