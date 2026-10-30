package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyDisabilityDto;
import com.electorama.msvc.survey.model.entity.SurveyDisabilityEntity;
import org.springframework.beans.BeanUtils;

public class SurveyDisabilityMapper
    extends BaseMapper<SurveyDisabilityEntity, SurveyDisabilityDto> {

  @Override
  public SurveyDisabilityEntity convertToEntity(SurveyDisabilityDto dto, Object... args) {
    SurveyDisabilityEntity entity = new SurveyDisabilityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyDisabilityDto convertToDto(SurveyDisabilityEntity entity, Object... args) {
    SurveyDisabilityDto dto = new SurveyDisabilityDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
