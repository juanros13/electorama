package com.electorama.msvc.followers.exeptions;

public class InvalidEntityException extends RuntimeException {
  public InvalidEntityException(String message) {
    super(message);
  }
}
