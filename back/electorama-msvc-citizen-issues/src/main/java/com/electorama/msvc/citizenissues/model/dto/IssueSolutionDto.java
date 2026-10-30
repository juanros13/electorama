package com.electorama.msvc.citizenissues.model.dto;

import java.util.Date;
import lombok.Data;

@Data
public class IssueSolutionDto {
  private Long id;
  private Integer quantity;
  private Long followerId;
  private Date solutionDate;
  private String comments;
  private IssueSolutionProviderDto issueSolutionProvider;
  private IssueSolutionResourceDto issueSolutionResource;
  private Long issueDetailId;
}
