package com.electorama.elections.models.dto;

import java.util.Date;
import lombok.Data;

@Data
public class PollingBoothIssuesDetailDto {
  private Long id;

  private String Description;

  private PollingBoothIssuesSubCategoryDto pollingBoothIssuesSubCategory;

  private Boolean isSynchronized;

  private Date created;
  private Date updated;
  private Date deleted;
}
