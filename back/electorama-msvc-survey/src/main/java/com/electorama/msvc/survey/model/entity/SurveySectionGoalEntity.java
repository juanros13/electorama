package com.electorama.msvc.survey.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_section_goal")
public class SurveySectionGoalEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String municipalityName;
  private Long municipalityId;
  private Long totalHomes;
  private Integer sectionNumber;
  private Integer sectionId;
  private Long goal;
}
