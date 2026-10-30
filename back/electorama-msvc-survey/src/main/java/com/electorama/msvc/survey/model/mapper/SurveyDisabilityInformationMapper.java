package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyDisabilityInformationDto;
import com.electorama.msvc.survey.model.entity.SurveyDisabilityInformationEntity;
import org.springframework.beans.BeanUtils;

public class SurveyDisabilityInformationMapper
    extends BaseMapper<SurveyDisabilityInformationEntity, SurveyDisabilityInformationDto> {

  @Override
  public SurveyDisabilityInformationEntity convertToEntity(
      SurveyDisabilityInformationDto dto, Object... args) {
    SurveyDisabilityInformationEntity entity = new SurveyDisabilityInformationEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyDisabilityInformationDto convertToDto(
      SurveyDisabilityInformationEntity entity, Object... args) {
    SurveyDisabilityInformationDto dto = new SurveyDisabilityInformationDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
