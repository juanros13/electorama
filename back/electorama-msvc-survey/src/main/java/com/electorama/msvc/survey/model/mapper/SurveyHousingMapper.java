package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingMapper extends BaseMapper<SurveyHousingEntity, SurveyHousingDto> {

  @Override
  public SurveyHousingEntity convertToEntity(SurveyHousingDto dto, Object... args) {
    SurveyHousingEntity entity = new SurveyHousingEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingDto convertToDto(SurveyHousingEntity entity, Object... args) {
    SurveyHousingDto dto = new SurveyHousingDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
