package com.electorama.msvc.electionsresults.models.dto;

import java.util.List;
import lombok.Data;

@Data
public class ResultDto {

  private Long id;
  private Long pollingBoothId;
  private String pathSheet;
  private List<ElectionResultDto> electionResultDtoList;
}
