package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingConditionDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingConditionEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingConditionMapper
    extends BaseMapper<SurveyHousingConditionEntity, SurveyHousingConditionDto> {

  @Override
  public SurveyHousingConditionEntity convertToEntity(
      SurveyHousingConditionDto dto, Object... args) {
    SurveyHousingConditionEntity entity = new SurveyHousingConditionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingConditionDto convertToDto(
      SurveyHousingConditionEntity entity, Object... args) {
    SurveyHousingConditionDto dto = new SurveyHousingConditionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
