package com.electorama.elections.models.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "candidates")
public class CandidateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String names;

  @NotBlank private String lastNames;

  @NotBlank private String surNames;

  private String candidatePath;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "election_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ElectionEntity election;
}
