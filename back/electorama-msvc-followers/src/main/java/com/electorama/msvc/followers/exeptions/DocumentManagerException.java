package com.electorama.msvc.followers.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentManagerException extends RuntimeException {
  private String code;
  private String message;

  public DocumentManagerException(String message) {
    super(message);
  }
}
