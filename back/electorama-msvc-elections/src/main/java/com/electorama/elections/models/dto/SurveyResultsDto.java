package com.electorama.elections.models.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class SurveyResultsDto {

  private LocalDateTime dateCollected;
  private List<CandidateAndMunicipalityDto> result;
}
