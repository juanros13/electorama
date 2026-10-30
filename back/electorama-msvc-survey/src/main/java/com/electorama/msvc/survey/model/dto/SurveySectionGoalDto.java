package com.electorama.msvc.survey.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveySectionGoalDto {
  private Long id;
  private String municipalityName;
  private Long municipalityId;
  private Long totalHomes;
  private Integer sectionId;
  private Integer sectionNumber;
  private Long goal;
}
