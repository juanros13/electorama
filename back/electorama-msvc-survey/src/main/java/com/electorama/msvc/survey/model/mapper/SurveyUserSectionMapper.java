package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyUserSectionDto;
import com.electorama.msvc.survey.model.entity.SurveyUserSectionEntity;
import org.springframework.beans.BeanUtils;

public class SurveyUserSectionMapper
    extends BaseMapper<SurveyUserSectionEntity, SurveyUserSectionDto> {

  @Override
  public SurveyUserSectionEntity convertToEntity(SurveyUserSectionDto dto, Object... args) {
    SurveyUserSectionEntity entity = new SurveyUserSectionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyUserSectionDto convertToDto(SurveyUserSectionEntity entity, Object... args) {
    SurveyUserSectionDto dto = new SurveyUserSectionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
