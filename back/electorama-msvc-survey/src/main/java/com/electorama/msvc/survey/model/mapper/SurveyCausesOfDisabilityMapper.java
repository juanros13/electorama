package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyCausesOfDisabilityDto;
import com.electorama.msvc.survey.model.entity.SurveyCausesOfDisabilityEntity;
import org.springframework.beans.BeanUtils;

public class SurveyCausesOfDisabilityMapper
    extends BaseMapper<SurveyCausesOfDisabilityEntity, SurveyCausesOfDisabilityDto> {

  @Override
  public SurveyCausesOfDisabilityEntity convertToEntity(
      SurveyCausesOfDisabilityDto dto, Object... args) {
    SurveyCausesOfDisabilityEntity entity = new SurveyCausesOfDisabilityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyCausesOfDisabilityDto convertToDto(
      SurveyCausesOfDisabilityEntity entity, Object... args) {
    SurveyCausesOfDisabilityDto dto = new SurveyCausesOfDisabilityDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
