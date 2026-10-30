package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyFamilyRelationshipDto;
import com.electorama.msvc.survey.model.entity.SurveyFamilyRelationshipEntity;
import org.springframework.beans.BeanUtils;

public class SurveyFamilyRelationshipMapper
    extends BaseMapper<SurveyFamilyRelationshipEntity, SurveyFamilyRelationshipDto> {
  @Override
  public SurveyFamilyRelationshipEntity convertToEntity(
      SurveyFamilyRelationshipDto dto, Object... args) {
    SurveyFamilyRelationshipEntity entity = new SurveyFamilyRelationshipEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyFamilyRelationshipDto convertToDto(
      SurveyFamilyRelationshipEntity entity, Object... args) {
    SurveyFamilyRelationshipDto dto = new SurveyFamilyRelationshipDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
