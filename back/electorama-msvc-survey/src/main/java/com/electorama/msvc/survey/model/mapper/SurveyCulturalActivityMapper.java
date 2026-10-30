package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyCulturalActivityDto;
import com.electorama.msvc.survey.model.entity.SurveyCulturalActivityEntity;
import org.springframework.beans.BeanUtils;

public class SurveyCulturalActivityMapper
    extends BaseMapper<SurveyCulturalActivityEntity, SurveyCulturalActivityDto> {

  @Override
  public SurveyCulturalActivityEntity convertToEntity(
      SurveyCulturalActivityDto dto, Object... args) {
    SurveyCulturalActivityEntity entity = new SurveyCulturalActivityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyCulturalActivityDto convertToDto(
      SurveyCulturalActivityEntity entity, Object... args) {
    SurveyCulturalActivityDto dto = new SurveyCulturalActivityDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
