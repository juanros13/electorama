package com.electorama.msvc.citizenissues.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues_categories")
public class IssueCategoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_category_subject_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueCategorySubjectEntity issueCategorySubject;
}
