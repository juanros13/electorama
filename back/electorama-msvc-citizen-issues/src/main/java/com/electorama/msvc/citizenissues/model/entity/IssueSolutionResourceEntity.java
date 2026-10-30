package com.electorama.msvc.citizenissues.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues_solutions_resources")
public class IssueSolutionResourceEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_category_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueCategoryEntity issueCategory;
}
