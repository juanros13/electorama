package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyFederalProgramDto;
import com.electorama.msvc.survey.model.entity.SurveyFederalProgramEntity;
import org.springframework.beans.BeanUtils;

public class SurveyFederalProgramMapper
    extends BaseMapper<SurveyFederalProgramEntity, SurveyFederalProgramDto> {

  @Override
  public SurveyFederalProgramEntity convertToEntity(SurveyFederalProgramDto dto, Object... args) {
    SurveyFederalProgramEntity entity = new SurveyFederalProgramEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyFederalProgramDto convertToDto(SurveyFederalProgramEntity entity, Object... args) {
    SurveyFederalProgramDto dto = new SurveyFederalProgramDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
