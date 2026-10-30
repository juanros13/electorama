package com.electorama.msvc.survey.model.dto.request;

import lombok.Data;

@Data
public class RequestSurveysAlterFolios {

  private String surveyUserAuthOuthId;
  private String surveyUserAuthInId;
  private Integer sectionOutId;

  private Integer folioStart;
  private Integer folioEnd;
}
