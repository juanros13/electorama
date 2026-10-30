package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class CoordinateMunicipalityDto {

  private Long id;

  private Long surveyUserId;
  private String latitude;
  private String longitude;
}
