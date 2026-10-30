package com.electorama.msvc.survey.exeptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleDashSurveyException extends RuntimeException {
  private String code;
  private String message;

  public SimpleDashSurveyException(String message) {
    super(message);
  }
}
