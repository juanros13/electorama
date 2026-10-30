package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ElectionMunicipalityDto {
  private Long id;
  private Long electionId;

  private Long municipalityId;

  private Long candidateId;
}
