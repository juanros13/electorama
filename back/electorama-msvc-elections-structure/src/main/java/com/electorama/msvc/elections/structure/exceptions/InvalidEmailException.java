package com.electorama.msvc.elections.structure.exceptions;

public class InvalidEmailException extends SimpleElectionEstructureException {
  public InvalidEmailException(String message, String code) {
    super(message, code);
  }
}
