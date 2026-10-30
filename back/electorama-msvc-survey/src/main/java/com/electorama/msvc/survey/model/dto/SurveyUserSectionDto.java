package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class SurveyUserSectionDto {

  private Long id;
  private Long surveyUserId;
  private Integer sectionId;
  private Integer sectionNumber;
  private Boolean isOwner;
}
