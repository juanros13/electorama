package com.electorama.elections.models.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PollingBoothIssuesDescriptionDto {
  private Long id;

  private String Municipality;
  private Long federalDistrict;
  private Long localDistrict;
  private Integer sectionNumber;
  private Long pollingBoothsId;
  private String pollingBoothName;
  private String nameCat;
  private String nameSub;
  private String description;
  private Date created;
}
