package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyAnnexDto;
import com.electorama.msvc.survey.model.entity.SurveyAnnexEntity;
import org.springframework.beans.BeanUtils;

public class SurveyAnnexMapper extends BaseMapper<SurveyAnnexEntity, SurveyAnnexDto> {

  private final SurveyIndigenousLanguageMapper surveyIndigenousLanguageMapper =
      new SurveyIndigenousLanguageMapper();

  private final SurveyOriginOfDisabilityMapper surveyOriginOfDisabilityMapper =
      new SurveyOriginOfDisabilityMapper();

  private final SurveyDisabilityInformationMapper surveyDisabilityInformationMapper =
      new SurveyDisabilityInformationMapper();

  private final SurveyTypeOfDisabilityMapper surveyTypeOfDisabilityMapper =
      new SurveyTypeOfDisabilityMapper();

  private final SurveyCausesOfDisabilityMapper surveyCausesOfDisabilityMapper =
      new SurveyCausesOfDisabilityMapper();

  private final SurveyTutorOrAssistantMapper surveyTutorOrAssistantMapper =
      new SurveyTutorOrAssistantMapper();

  private final SurveyRequestingOrAuxiliaryDocumentMapper
      surveyRequestingOrAuxiliaryDocumentMapper = new SurveyRequestingOrAuxiliaryDocumentMapper();

  private final SurveyFamilyRelationshipMapper surveyFamilyRelationshipMapper =
      new SurveyFamilyRelationshipMapper();

  @Override
  public SurveyAnnexEntity convertToEntity(SurveyAnnexDto dto, Object... args) {
    SurveyAnnexEntity entity = new SurveyAnnexEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getSurveyPersonId() != null) {
        entity.setSurveyPersonId(dto.getSurveyPersonId());
      }
      if (dto.getPathFileAddressApplicant() != null) {
        entity.setPathFileAddressApplicant(dto.getPathFileAddressApplicant());
      }
      if (dto.getPathFileAddressAssistant() != null) {
        entity.setPathFileAddressAssistant(dto.getPathFileAddressAssistant());
      }
      if (dto.getCausesDisability() != null) {
        entity.setCausesDisability(dto.getCausesDisability());
      }
      if (dto.getDesabilityOrigin() != null) {
        entity.setDesabilityOrigin(dto.getDesabilityOrigin());
      }
      if (dto.getDesabilityType() != null) {
        entity.setDesabilityType(dto.getDesabilityType());
      }
      if (dto.getDesabililtyInformation() != null) {
        entity.setDesabililtyInformation(dto.getDesabililtyInformation());
      }
      if (dto.getEthnicOrAfromexicanGroup() != null) {
        entity.setEthnicOrAfromexicanGroup(dto.getEthnicOrAfromexicanGroup());
      }
      if (dto.getPathFileBirthCertificateApplicant() != null) {
        entity.setPathFileBirthCertificateApplicant(dto.getPathFileBirthCertificateApplicant());
      }

      if (dto.getSurveyIndigenousLanguage() != null) {
        entity.setSurveyIndigenousLanguage(
            surveyIndigenousLanguageMapper.convertToEntity(dto.getSurveyIndigenousLanguage()));
      }
      if (dto.getSurveyOriginOfDisability() != null) {
        entity.setSurveyOriginOfDisability(
            surveyOriginOfDisabilityMapper.convertToEntity(dto.getSurveyOriginOfDisability()));
      }
      if (dto.getSurveyDisabilityInformation() != null) {
        entity.setSurveyDisabilityInformation(
            surveyDisabilityInformationMapper.convertToEntity(
                dto.getSurveyDisabilityInformation()));
      }
      if (dto.getSurveyTypeOfDisability() != null) {
        entity.setSurveyTypeOfDisability(
            surveyTypeOfDisabilityMapper.convertToEntity(dto.getSurveyTypeOfDisability()));
      }
      if (dto.getSurveyCausesOfDisability() != null) {
        entity.setSurveyCausesOfDisability(
            surveyCausesOfDisabilityMapper.convertToEntity(dto.getSurveyCausesOfDisability()));
      }
      if (dto.getSurveyTutorOrAssistant() != null) {
        entity.setSurveyTutorOrAssistant(
            surveyTutorOrAssistantMapper.convertToEntity(dto.getSurveyTutorOrAssistant()));
      }
      if (dto.getSurveyRequestingOrAuxiliaryDocument() != null) {
        entity.setSurveyRequestingOrAuxiliaryDocument(
            surveyRequestingOrAuxiliaryDocumentMapper.convertToEntity(
                dto.getSurveyRequestingOrAuxiliaryDocument()));
      }

      if (dto.getFamilyRelationship() != null) {
        entity.setFamilyRelationship(
            surveyFamilyRelationshipMapper.convertToEntity(dto.getFamilyRelationship()));
      }
    }
    return entity;
  }

  @Override
  public SurveyAnnexDto convertToDto(SurveyAnnexEntity entity, Object... args) {
    SurveyAnnexDto dto = new SurveyAnnexDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);

      if (entity.getSurveyIndigenousLanguage() != null) {
        dto.setSurveyIndigenousLanguage(
            surveyIndigenousLanguageMapper.convertToDto(entity.getSurveyIndigenousLanguage()));
      }
      if (entity.getSurveyOriginOfDisability() != null) {
        dto.setSurveyOriginOfDisability(
            surveyOriginOfDisabilityMapper.convertToDto(entity.getSurveyOriginOfDisability()));
      }
      if (entity.getSurveyDisabilityInformation() != null) {
        dto.setSurveyDisabilityInformation(
            surveyDisabilityInformationMapper.convertToDto(
                entity.getSurveyDisabilityInformation()));
      }
      if (entity.getSurveyTypeOfDisability() != null) {
        dto.setSurveyTypeOfDisability(
            surveyTypeOfDisabilityMapper.convertToDto(entity.getSurveyTypeOfDisability()));
      }
      if (entity.getSurveyCausesOfDisability() != null) {
        dto.setSurveyCausesOfDisability(
            surveyCausesOfDisabilityMapper.convertToDto(entity.getSurveyCausesOfDisability()));
      }
      if (entity.getSurveyTutorOrAssistant() != null) {
        dto.setSurveyTutorOrAssistant(
            surveyTutorOrAssistantMapper.convertToDto(entity.getSurveyTutorOrAssistant()));
      }
      if (entity.getSurveyRequestingOrAuxiliaryDocument() != null) {
        dto.setSurveyRequestingOrAuxiliaryDocument(
            surveyRequestingOrAuxiliaryDocumentMapper.convertToDto(
                entity.getSurveyRequestingOrAuxiliaryDocument()));
      }

      if (entity.getFamilyRelationship() != null) {
        dto.setFamilyRelationship(
            surveyFamilyRelationshipMapper.convertToDto(entity.getFamilyRelationship()));
      }
    }
    return dto;
  }
}
