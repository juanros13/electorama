package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ElectionLocalDistrictDto {
  private Long id;
  private Long electionId;

  private Long localDistrictId;

  private Long candidateId;
}
