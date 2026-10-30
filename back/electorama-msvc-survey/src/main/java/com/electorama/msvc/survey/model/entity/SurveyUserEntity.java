package com.electorama.msvc.survey.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_user")
public class SurveyUserEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String deviceId;
  private String name;
  private String authId;
  private String userName;
  private Long userPersonId;
  private Long municipalyId;
  private String municipalityName;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "surveyUserId", cascade = CascadeType.MERGE)
  @JsonIgnoreProperties(
      value = {"survey", "hibernateLazyInitializer", "handler"},
      allowSetters = true)
  private List<SurveyUserSectionEntity> surveyUserSection;

  private String surveyMunicipalityAuthId;
}
