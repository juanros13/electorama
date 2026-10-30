package com.electorama.msvc.followers.model.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Data;

@Data
public class PoliticalPartyDto {

  private Long id;

  @NotBlank private String name;

  @NotBlank private String acronym;

  @NotBlank private String logo;
  private PoliticalPartyTypeDto politicalPartyType;

  private List<CandidateDto> candidate;
}
