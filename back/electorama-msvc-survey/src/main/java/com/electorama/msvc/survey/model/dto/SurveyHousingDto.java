package com.electorama.msvc.survey.model.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyHousingDto {
  private Long id;
  private String name;
}
