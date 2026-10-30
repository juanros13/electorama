package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyRequestingOrAuxiliaryDocumentDto;
import com.electorama.msvc.survey.model.entity.SurveyRequestingOrAuxiliaryDocumentEntity;
import org.springframework.beans.BeanUtils;

public class SurveyRequestingOrAuxiliaryDocumentMapper
    extends BaseMapper<
        SurveyRequestingOrAuxiliaryDocumentEntity, SurveyRequestingOrAuxiliaryDocumentDto> {

  @Override
  public SurveyRequestingOrAuxiliaryDocumentEntity convertToEntity(
      SurveyRequestingOrAuxiliaryDocumentDto dto, Object... args) {
    SurveyRequestingOrAuxiliaryDocumentEntity entity =
        new SurveyRequestingOrAuxiliaryDocumentEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyRequestingOrAuxiliaryDocumentDto convertToDto(
      SurveyRequestingOrAuxiliaryDocumentEntity entity, Object... args) {
    SurveyRequestingOrAuxiliaryDocumentDto dto = new SurveyRequestingOrAuxiliaryDocumentDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
