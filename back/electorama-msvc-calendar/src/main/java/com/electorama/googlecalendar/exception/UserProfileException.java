package com.electorama.googlecalendar.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileException extends RuntimeException {

  private String code;
  private String message;

  public UserProfileException(String message) {
    super(message);
  }
}
