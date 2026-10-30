package com.electorama.msvc.survey.model.dto.request;

import lombok.Data;

@Data
public class RequestFolios {
  private Long surveyUserId;
  private Integer folioStart;
  private Integer folioEnd;
}
