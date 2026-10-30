package com.electorama.msvc.survey.model.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class DateDto {

  private LocalDate startDate;
  private LocalDate endDate;
}
