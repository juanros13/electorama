package com.electorama.elections.models.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PollingBoothIssuesDto {
  private Long id;

  private Long pollingBoothId;

  private String Description;

  private PollingBoothIssuesSubCategoryDto pollingBoothIssuesSubCategory;

  private Boolean isSynchronized;

  private Date created;
  private Date updated;
  private Date deleted;
}
