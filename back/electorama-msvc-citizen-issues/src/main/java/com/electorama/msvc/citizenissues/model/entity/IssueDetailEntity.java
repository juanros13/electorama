package com.electorama.msvc.citizenissues.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues_details")
public class IssueDetailEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String comments;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_id")
  @JsonIgnoreProperties({"issueDetailEntities", "hibernateLazyInitializer", "handler"})
  private IssueEntity issue;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_type_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueTypeEntity issueType;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_category_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueCategoryEntity issueCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_status_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueStatusEntity issueStatus;

  @OneToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "issue_solution_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueSolutionEntity issueSolution;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(name = "issue_solution_resource_tmp_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueSolutionResourceEntity issueSolutionResourceTmp;

  @ManyToOne(fetch = FetchType.EAGER, optional = true)
  @JoinColumn(
      name = "issues_solutions_documents_categories",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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
