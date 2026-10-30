package com.electorama.msvc.survey.model.dto;

import lombok.Data;

@Data
public class AuthIdMunicipalityDto {

  private Long id;
  private Long municipalityId;
  private String municipalityName;
  private Long sectionNumber;
  private Long goalHolmes;
  private Long folioCaptured;
  private Long formCaptured;
  private String authId;
  private String name;
}
