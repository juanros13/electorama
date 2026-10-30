package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyOriginOfDisabilityDto;
import com.electorama.msvc.survey.model.entity.SurveyOriginOfDisabilityEntity;
import org.springframework.beans.BeanUtils;

public class SurveyOriginOfDisabilityMapper
    extends BaseMapper<SurveyOriginOfDisabilityEntity, SurveyOriginOfDisabilityDto> {

  @Override
  public SurveyOriginOfDisabilityEntity convertToEntity(
      SurveyOriginOfDisabilityDto dto, Object... args) {
    SurveyOriginOfDisabilityEntity entity = new SurveyOriginOfDisabilityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyOriginOfDisabilityDto convertToDto(
      SurveyOriginOfDisabilityEntity entity, Object... args) {
    SurveyOriginOfDisabilityDto dto = new SurveyOriginOfDisabilityDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
