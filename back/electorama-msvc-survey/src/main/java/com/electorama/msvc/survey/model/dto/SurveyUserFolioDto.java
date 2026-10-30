package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class SurveyUserFolioDto {

  private Long id;
  private String folio;
  private Integer surveyUserId;
  private String comment;
}
