package com.electorama.msvc.citizenissues.model.dto;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionDocumentsCategoryEntity;
import java.util.Date;
import lombok.Data;

@Data
public class IssueDetailDto {

  private Long id;
  private String comments;
  private IssueDto issue;
  private IssueTypeDto issueType;
  private IssueCategoryDto issueCategory;
  private IssueStatusDto issueStatus;
  private IssueSolutionDto issueSolution;
  private IssueSolutionResourceDto issueSolutionResourceTmp;
  private IssueSolutionDocumentsCategoryEntity IssueSolutionDocumentsCategory;
  private String userCreateId;
  private Date registrationDate;
  private Date deletedDate;
  private String name;
  private String lastName;
  private String surName;
  private String curp;
  private String birtDay;
  private Boolean urgent;
  private String pathFile;
  private String uuid;
}
