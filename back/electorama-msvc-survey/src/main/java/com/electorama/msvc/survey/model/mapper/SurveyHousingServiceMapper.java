package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingServicesDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingServicesEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingServiceMapper
    extends BaseMapper<SurveyHousingServicesEntity, SurveyHousingServicesDto> {

  @Override
  public SurveyHousingServicesEntity convertToEntity(SurveyHousingServicesDto dto, Object... args) {
    SurveyHousingServicesEntity entity = new SurveyHousingServicesEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingServicesDto convertToDto(SurveyHousingServicesEntity entity, Object... args) {
    SurveyHousingServicesDto dto = new SurveyHousingServicesDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
