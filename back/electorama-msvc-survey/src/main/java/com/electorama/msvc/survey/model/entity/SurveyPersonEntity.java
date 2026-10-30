package com.electorama.msvc.survey.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "survey_person")
public class SurveyPersonEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // Todas las personas
  private String names;
  private String lastName;
  private String surName;
  private String dateOfBirth;
  private String deleted;
  private String gender;
  private String isDisabled;
  private String ethnicGroup;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_job_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyJobEntity surveyJob;

  private String otherSurveyJob;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_profession_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyProfessionEntity surveyProfession;

  private String otherSurveyProfession;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_federal_program_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyFederalProgramEntity surveyFederalProgram;

  private String otherSurveyFederalProgram;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_family_relationship_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyFamilyRelationshipEntity surveyFamilyRelationship;

  private String otherDisabledName;
  private String landlinePhone;
  private String mobilePhone;
  private String curp;
  private String identificationDocument;
  private String noIdentificationDocument;

  private String afroMexicanGroup;

  private Integer income;

  private Boolean isHeadOfHousehold;

  private String timeActivity;
  private String ownBusiness;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_id")
  @JsonIgnoreProperties({"surveyPersonEntityList", "hibernateLazyInitializer", "handler"})
  private SurveyEntity survey;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "survey_disable_id")
  @JsonIgnoreProperties({"surveyPersonEntityList", "hibernateLazyInitializer", "handler"})
  private SurveyDisabilityEntity isDisabledName;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "surveyPersonId", cascade = CascadeType.ALL)
  @JsonIgnoreProperties(
      value = {"Annex", "hibernateLazyInitializer", "handler"},
      allowSetters = true)
  private List<SurveyAnnexEntity> surveyAnnex = new ArrayList<>();
}
