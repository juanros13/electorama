package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingDocumentDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingDocumentEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingDocumentMapper
    extends BaseMapper<SurveyHousingDocumentEntity, SurveyHousingDocumentDto> {

  @Override
  public SurveyHousingDocumentEntity convertToEntity(SurveyHousingDocumentDto dto, Object... args) {
    SurveyHousingDocumentEntity entity = new SurveyHousingDocumentEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingDocumentDto convertToDto(SurveyHousingDocumentEntity entity, Object... args) {
    SurveyHousingDocumentDto dto = new SurveyHousingDocumentDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
