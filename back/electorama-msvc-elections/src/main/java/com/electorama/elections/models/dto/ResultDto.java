package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ResultDto {
  private Long candidateId;

  private String politicalPartyName;
  private Long result;
  private Integer softorder;
}
