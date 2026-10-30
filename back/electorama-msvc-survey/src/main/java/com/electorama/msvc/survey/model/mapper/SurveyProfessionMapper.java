package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyProfessionDto;
import com.electorama.msvc.survey.model.entity.SurveyProfessionEntity;
import org.springframework.beans.BeanUtils;

public class SurveyProfessionMapper
    extends BaseMapper<SurveyProfessionEntity, SurveyProfessionDto> {

  @Override
  public SurveyProfessionEntity convertToEntity(SurveyProfessionDto dto, Object... args) {
    SurveyProfessionEntity entity = new SurveyProfessionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyProfessionDto convertToDto(SurveyProfessionEntity entity, Object... args) {
    SurveyProfessionDto dto = new SurveyProfessionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
