package com.electorama.msvc.survey.model.dto;

import java.util.List;
import lombok.Data;

@Data
public class SurveyUserSectionListDto {

  private Long id;
  private String userName;
  private String name;
  private String deviceId;
  private String authId;

  private List<SurveyUserSectionDto> surveyUserSection;
}
