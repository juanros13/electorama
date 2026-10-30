package com.electorama.msvc.survey.model.dto;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyHousingServicesDto {
  private Long id;
  private String name;

  private List<SurveyDto> surveyEntities;
}
