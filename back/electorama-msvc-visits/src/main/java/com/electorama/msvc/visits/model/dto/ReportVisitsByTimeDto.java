package com.electorama.msvc.visits.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReportVisitsByTimeDto {

  private String dateVisits;
  private String totalVisits;
}
