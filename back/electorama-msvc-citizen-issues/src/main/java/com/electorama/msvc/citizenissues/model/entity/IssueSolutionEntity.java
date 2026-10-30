package com.electorama.msvc.citizenissues.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "issues_solutions")
public class IssueSolutionEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_solution_provider_id", referencedColumnName = "id")
  private IssueSolutionProviderEntity issueSolutionProvider;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "issue_solution_resource_id", referencedColumnName = "id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private IssueSolutionResourceEntity issueSolutionResource;

  private Integer quantity;

  private Long followerId;

  private Date solutionDate;

  private String comments;
}
