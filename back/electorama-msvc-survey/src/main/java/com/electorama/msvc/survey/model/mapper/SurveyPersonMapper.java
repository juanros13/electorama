package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyPersonDto;
import com.electorama.msvc.survey.model.entity.SurveyPersonEntity;
import org.springframework.beans.BeanUtils;

public class SurveyPersonMapper extends BaseMapper<SurveyPersonEntity, SurveyPersonDto> {
  private final SurveyJobMapper surveyJobMapper = new SurveyJobMapper();
  private final SurveyProfessionMapper surveyProfessionMapper = new SurveyProfessionMapper();
  private final SurveyFamilyRelationshipMapper surveyFamilyRelationshipMapper =
      new SurveyFamilyRelationshipMapper();
  private final SurveyFederalProgramMapper surveyFederalProgramMapper =
      new SurveyFederalProgramMapper();
  private final SurveyDisabilityMapper disabilityMapper = new SurveyDisabilityMapper();

  private final SurveyAnnexMapper surveyAnnexMapper = new SurveyAnnexMapper();

  @Override
  public SurveyPersonEntity convertToEntity(SurveyPersonDto dto, Object... args) {
    SurveyPersonEntity entity = new SurveyPersonEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getSurveyJob() != null) {
        entity.setSurveyJob(surveyJobMapper.convertToEntity(dto.getSurveyJob()));
      }
      if (dto.getSurveyProfession() != null) {
        entity.setSurveyProfession(
            surveyProfessionMapper.convertToEntity(dto.getSurveyProfession()));
      }
      if (dto.getSurveyFederalProgram() != null) {
        entity.setSurveyFederalProgram(
            surveyFederalProgramMapper.convertToEntity(dto.getSurveyFederalProgram()));
      }
      if (dto.getFamilyRelationship() != null) {
        entity.setSurveyFamilyRelationship(
            surveyFamilyRelationshipMapper.convertToEntity(dto.getFamilyRelationship()));
      }
      if (dto.getIsDisabledName() != null) {
        entity.setIsDisabledName(disabilityMapper.convertToEntity(dto.getIsDisabledName()));
      }
      if (dto.getSurveyAnnex() != null) {
        entity.setSurveyAnnex(surveyAnnexMapper.convertToEntityList(dto.getSurveyAnnex()));
      }
    }
    return entity;
  }

  @Override
  public SurveyPersonDto convertToDto(SurveyPersonEntity entity, Object... args) {
    SurveyPersonDto dto = new SurveyPersonDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getSurveyJob() != null) {
        dto.setSurveyJob(surveyJobMapper.convertToDto(entity.getSurveyJob()));
      }
      if (entity.getSurveyProfession() != null) {
        dto.setSurveyProfession(surveyProfessionMapper.convertToDto(entity.getSurveyProfession()));
      }
      if (entity.getSurveyFederalProgram() != null) {
        dto.setSurveyFederalProgram(
            surveyFederalProgramMapper.convertToDto(entity.getSurveyFederalProgram()));
      }
      if (entity.getSurveyFamilyRelationship() != null) {
        dto.setFamilyRelationship(
            surveyFamilyRelationshipMapper.convertToDto(entity.getSurveyFamilyRelationship()));
      }
      if (entity.getIsDisabledName() != null) {
        dto.setIsDisabledName(disabilityMapper.convertToDto(entity.getIsDisabledName()));
      }
      if (entity.getSurveyAnnex() != null) {
        dto.setSurveyAnnex(surveyAnnexMapper.convertToDtoList(entity.getSurveyAnnex()));
      }
    }
    return dto;
  }
}
