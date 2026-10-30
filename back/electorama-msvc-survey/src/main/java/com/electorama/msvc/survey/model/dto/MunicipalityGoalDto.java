package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class MunicipalityGoalDto {

  private Long id;
  private Long municipalityId;
  private String municipalityName;
  private Long stateServer;
  private Long goalHolmes;
  private Long folioCaptured;
  private Long formCaptured;
  private Long homesToBeCensuced;
}
