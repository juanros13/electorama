package com.electorama.msvc.citizenissues.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDashCitizenIssuesException extends RuntimeException {
  private String code;
  private String message;

  public SimpleDashCitizenIssuesException(String message) {
    super(message);
  }
}
