package com.electorama.msvc.electionsresults.models.rest;

import java.util.Date;
import lombok.Data;

@Data
public class PollingBoothElectionAssociationResponse {
  private Long id;

  private Long pollingBoothId;

  private Long electionId;

  private Boolean isSynchronized;

  private String pathSheet;
  private Date pathSheetCreated;
  private Date pathSheetUpdated;
  private Boolean isValidatedSheet;
}
