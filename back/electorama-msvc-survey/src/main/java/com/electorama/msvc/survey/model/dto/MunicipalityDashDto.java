package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class MunicipalityDashDto {

  private Long id;
  private Long municipalityId;
  private String municipalityName;
  private Long goalHolmes;
  private Long formCaptured;
  private Long formToCapture;
}
