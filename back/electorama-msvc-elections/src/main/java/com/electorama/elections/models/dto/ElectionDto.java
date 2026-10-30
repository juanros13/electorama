package com.electorama.elections.models.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class ElectionDto {
  private Long id;

  @NotBlank private String name;

  private ElectionYearDto electionYear;
  private ElectionPositionDto electionPosition;
  private ElectionTypeDto electionType;
  private List<PoliticalPartyDto> politicalPartyList;
  private String candidatePath;

  private Integer voterList;
}
