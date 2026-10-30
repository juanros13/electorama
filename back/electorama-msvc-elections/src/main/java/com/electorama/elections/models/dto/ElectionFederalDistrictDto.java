package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ElectionFederalDistrictDto {
  private Long id;
  private Long electionId;

  private Long federalDistrictId;

  private Long candidateId;
}
