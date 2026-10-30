package com.electorama.msvc.survey.model.dto.request;

import lombok.Data;

@Data
public class RequestSections {

  private String surveyUserAuthOuthId;
  private String surveyUserAuthInId;
  private Integer sectionOutId;
}
