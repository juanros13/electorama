package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyTypeOfDisabilityDto;
import com.electorama.msvc.survey.model.entity.SurveyTypeOfDisabilityEntity;
import org.springframework.beans.BeanUtils;

public class SurveyTypeOfDisabilityMapper
    extends BaseMapper<SurveyTypeOfDisabilityEntity, SurveyTypeOfDisabilityDto> {

  @Override
  public SurveyTypeOfDisabilityEntity convertToEntity(
      SurveyTypeOfDisabilityDto dto, Object... args) {
    SurveyTypeOfDisabilityEntity entity = new SurveyTypeOfDisabilityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyTypeOfDisabilityDto convertToDto(
      SurveyTypeOfDisabilityEntity entity, Object... args) {
    SurveyTypeOfDisabilityDto dto = new SurveyTypeOfDisabilityDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
