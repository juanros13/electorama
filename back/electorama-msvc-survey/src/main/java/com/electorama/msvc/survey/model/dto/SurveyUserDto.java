package com.electorama.msvc.survey.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class SurveyUserDto {

  private Long id;
  private String deviceId;
  private String name;
  private String authId;
  private String userName;
  private Long userPersonId;
  private Long municipalyId;
  private String municipalityName;
  private List<SurveyUserSectionDto> surveyUserSection;
  private String surveyMunicipalityAuthId;
}
