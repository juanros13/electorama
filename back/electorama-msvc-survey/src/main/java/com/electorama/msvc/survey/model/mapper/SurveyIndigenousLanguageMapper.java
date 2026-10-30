package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyIndigenousLanguageDto;
import com.electorama.msvc.survey.model.entity.SurveyIndigenousLanguageEntity;
import org.springframework.beans.BeanUtils;

public class SurveyIndigenousLanguageMapper
    extends BaseMapper<SurveyIndigenousLanguageEntity, SurveyIndigenousLanguageDto> {

  @Override
  public SurveyIndigenousLanguageEntity convertToEntity(
      SurveyIndigenousLanguageDto dto, Object... args) {
    SurveyIndigenousLanguageEntity entity = new SurveyIndigenousLanguageEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyIndigenousLanguageDto convertToDto(
      SurveyIndigenousLanguageEntity entity, Object... args) {
    SurveyIndigenousLanguageDto dto = new SurveyIndigenousLanguageDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
