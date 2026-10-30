package com.electorama.elections.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class ElectionsAndCandidatesDto {
  private Long candidateId;
  private String electionName;
  private String electionPositionName;
  private String names;
  private String lastNames;
  private String surNames;
  private Integer totalVotes;
  private Long electionId;
  private String candidatePath;
  private List<ResultDto> resultList;
}
