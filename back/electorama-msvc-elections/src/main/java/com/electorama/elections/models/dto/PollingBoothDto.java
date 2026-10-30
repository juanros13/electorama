package com.electorama.elections.models.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;

@Data
public class PollingBoothDto {
  private Long id;

  private Integer seccionNumber;

  private Integer seccionId;

  private String pollingBoothId;

  private String state;

  private Integer stateId;

  private String municipality;

  private Integer municipalityId;

  private Integer federalDistrict;

  private String federalDistrictName;

  private Integer localDistrict;

  private String address;

  private String referenceAddress;

  private Integer votersRegistry;

  private Integer votersList;

  private String approvedBoard;

  private String approvedCommittee;

  private String pollingBoothKey;

  private String pollingBoothNumber;

  private PollingBoothAddressTypeDto pollingBoothAddressType;

  private PollingBoothTypeDto pollingBoothType;

  private PollingBoothCategoryDto pollingBoothCategory;

  private LocalDateTime dateStatusOpen;
  private LocalDateTime dateClosed;

  private PollingBoothStatusDto pollingBoothStatus;

  private String latitude;
  private String longitude;

  private List<PollingBoothIssuesDto> pollingBoothIssuesList;
}
