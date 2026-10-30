package com.electorama.msvc.survey.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "survey_annex")
public class SurveyAnnexEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private Long surveyPersonId;
  private Long surveyId;
  private String ethnicOrAfromexicanGroup;
  private String whichGroup;

  private String speaksIndigenousLanguage;
  private String otherSpeak;

  private String hasADisability;
  private Integer desabilityOrigin;
  private Integer desabililtyInformation;
  private Integer desabilityType;
  private Integer causesDisability;
  private String anotherCause;
  private String healthCertificate;
  private String WhichCertificate;

  private String requiriesHelp;
  private String namesAux;
  private String lastNameAux;
  private String surNameAux;
  private String phoneNumber;

  private String requestingDocumentIne;
  private String requestingDocumentMinutes;
  private String requestingDocumentCurp;
  private String requestingDocumentProofAddress;

  private String requestingAuxiliarIne;
  private String requestingAuxiliarMinutes;
  private String requestingAuxiliarCurp;
  private String requestingAuxiliarProofAddress;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_indigenous_language_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyIndigenousLanguageEntity surveyIndigenousLanguage;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_origin_of_disability_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyOriginOfDisabilityEntity surveyOriginOfDisability;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
      name = "survey_disability_information_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyDisabilityInformationEntity surveyDisabilityInformation;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_type_of_disability_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyTypeOfDisabilityEntity surveyTypeOfDisability;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_causes_of_disability_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyCausesOfDisabilityEntity surveyCausesOfDisability;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_tutor_or_assistant_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyTutorOrAssistantEntity surveyTutorOrAssistant;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(
      name = "survey_requesting_or_auxiliar_document_id",
      referencedColumnName = "id",
      nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyRequestingOrAuxiliaryDocumentEntity surveyRequestingOrAuxiliaryDocument;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "survey_family_relationship_id", referencedColumnName = "id", nullable = true)
  @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
  private SurveyFamilyRelationshipEntity familyRelationship;

  private String otherRelationShip;

  private String pathFileIdentificationApplicant;
  private String pathFileBirthCertificateApplicant;
  private String pathFileCurpApplicant;
  private String pathFileAddressApplicant;

  private String pathFileIdentificationAssistant;
  private String pathFileBirthCertificateAssistant;
  private String pathFileCurpAssistant;
  private String pathFileAddressAssistant;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  private Date updated;

  @Temporal(TemporalType.TIMESTAMP)
  private Date deleted;
}
