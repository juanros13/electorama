package com.electorama.msvc.elections.structure.exceptions;

public class UserAlreadyRegisteredException extends SimpleElectionEstructureException {
  public UserAlreadyRegisteredException(String message, String code) {
    super(message, code);
  }
}
