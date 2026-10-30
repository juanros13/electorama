package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyTutorOrAssistantDto;
import com.electorama.msvc.survey.model.entity.SurveyTutorOrAssistantEntity;
import org.springframework.beans.BeanUtils;

public class SurveyTutorOrAssistantMapper
    extends BaseMapper<SurveyTutorOrAssistantEntity, SurveyTutorOrAssistantDto> {

  @Override
  public SurveyTutorOrAssistantEntity convertToEntity(
      SurveyTutorOrAssistantDto dto, Object... args) {
    SurveyTutorOrAssistantEntity entity = new SurveyTutorOrAssistantEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyTutorOrAssistantDto convertToDto(
      SurveyTutorOrAssistantEntity entity, Object... args) {
    SurveyTutorOrAssistantDto dto = new SurveyTutorOrAssistantDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
