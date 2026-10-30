package com.electorama.msvc.survey.model.dto;

import com.electorama.msvc.survey.model.entity.*;
import java.util.Date;
import lombok.Data;

@Data
public class SurveyAnnexDto {

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
  private SurveyFamilyRelationshipDto familyRelationship;
  private String otherRelationShip;
  private String phoneNumber;
  private String requestingDocumentIne;
  private String requestingDocumentMinutes;
  private String requestingDocumentCurp;
  private String requestingDocumentProofAddress;

  private String requestingAuxiliarIne;
  private String requestingAuxiliarMinutes;
  private String requestingAuxiliarCurp;
  private String requestingAuxiliarProofAddress;

  private SurveyIndigenousLanguageDto surveyIndigenousLanguage;
  private SurveyOriginOfDisabilityDto surveyOriginOfDisability;
  private SurveyDisabilityInformationDto surveyDisabilityInformation;
  private SurveyTypeOfDisabilityDto surveyTypeOfDisability;
  private SurveyCausesOfDisabilityDto surveyCausesOfDisability;
  private SurveyTutorOrAssistantDto surveyTutorOrAssistant;
  private SurveyRequestingOrAuxiliaryDocumentDto surveyRequestingOrAuxiliaryDocument;

  private String pathFileIdentificationApplicant;
  private String pathFileBirthCertificateApplicant;
  private String pathFileCurpApplicant;
  private String pathFileAddressApplicant;

  private String pathFileIdentificationAssistant;
  private String pathFileBirthCertificateAssistant;
  private String pathFileCurpAssistant;
  private String pathFileAddressAssistant;

  private Date created;

  private Date updated;
  private Date deleted;
}
