package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyJobDto;
import com.electorama.msvc.survey.model.entity.SurveyJobEntity;
import org.springframework.beans.BeanUtils;

public class SurveyJobMapper extends BaseMapper<SurveyJobEntity, SurveyJobDto> {

  @Override
  public SurveyJobEntity convertToEntity(SurveyJobDto dto, Object... args) {
    SurveyJobEntity entity = new SurveyJobEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyJobDto convertToDto(SurveyJobEntity entity, Object... args) {
    SurveyJobDto dto = new SurveyJobDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
