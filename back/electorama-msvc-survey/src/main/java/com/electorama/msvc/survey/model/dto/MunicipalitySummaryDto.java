package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class MunicipalitySummaryDto {

  private Long id;
  private Long municipalityId;
  private String municipalityName;
  private Long section;
  private Long goalHolmes;
  private Long folioCaptured;
  private Long formCaptured;
}
