package com.electorama.elections.models.dto;

import lombok.Data;

@Data
public class PollingBoothResultDto {
  private Long id;

  private String electionName;
  private String electionYear;
  private String electionType;
  private String state;
  private String Municipality;
  private Long federalDistrict;
  private Long localDistrict;
  private Integer sectionNumber;
  private Long pollingBoothsId;
  private String statusPollingBooth;
  private String status;
  private Long pollingBoothsElectionAssociationId;
  private String pollingBoothName;
  private Integer votersList;
}
