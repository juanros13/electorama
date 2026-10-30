package com.electorama.elections.models.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PollingBoothElectionAssociationDto {
  private Long id;

  private Long pollingBoothId;

  private Long electionId;

  private Boolean isSynchronized;

  private String pathSheet;
  private Date pathSheetCreated;
  private Date pathSheetUpdated;
  private Boolean isValidatedSheet;
}
