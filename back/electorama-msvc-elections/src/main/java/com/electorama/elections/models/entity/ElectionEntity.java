package com.electorama.elections.models.entity;

import com.electorama.elections.models.enums.ElectionStatusEnum;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "elections")
public class ElectionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank private String name;

  @Enumerated(EnumType.STRING)
  private ElectionStatusEnum status;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "election_year_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ElectionYearEntity electionYear;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "election_position_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ElectionPositionEntity electionPosition;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "election_type_id")
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private ElectionTypeEntity electionType;

  private Integer voterList;
}
