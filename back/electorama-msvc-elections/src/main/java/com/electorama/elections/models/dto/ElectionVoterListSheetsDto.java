package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class ElectionVoterListSheetsDto {

  private Long id;
  private Integer electionId;
  private Integer total;
  private Integer progress;
}
