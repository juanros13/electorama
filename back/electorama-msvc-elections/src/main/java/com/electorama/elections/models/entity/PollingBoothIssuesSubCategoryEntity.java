package com.electorama.elections.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "polling_booths_issues_sub_categories")
public class PollingBoothIssuesSubCategoryEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private PollingBoothIssuesCategoryEntity pollingBoothIssuesCategory;
}
