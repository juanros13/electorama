package com.electorama.msvc.survey.model.dto;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SurveyPersonDto {
  private Long id;
  private String names;
  private String lastName;
  private String surName;
  private String dateOfBirth;
  private String deleted;
  private String gender;
  private String landlinePhone;
  private String mobilePhone;
  private String curp;
  private String identificationDocument;
  private String noIdentificationDocument;

  private String ethnicGroup;
  private String afroMexicanGroup;
  private String isDisabled;

  private Integer income;

  private Boolean isHeadOfHousehold;

  private String timeActivity;
  private String ownBusiness;
  private Long surveyId;

  private SurveyJobDto surveyJob;
  private String otherSurveyJob;

  private SurveyProfessionDto surveyProfession;
  private String otherSurveyProfession;

  private SurveyFederalProgramDto surveyFederalProgram;
  private String otherSurveyFederalProgram;

  private SurveyFamilyRelationshipDto familyRelationship;

  private SurveyDisabilityDto isDisabledName;

  private List<SurveyAnnexDto> surveyAnnex;
}
