package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingMaterialWallDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingMaterialWallEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingMaterialWallMapper
    extends BaseMapper<SurveyHousingMaterialWallEntity, SurveyHousingMaterialWallDto> {

  @Override
  public SurveyHousingMaterialWallEntity convertToEntity(
      SurveyHousingMaterialWallDto dto, Object... args) {
    SurveyHousingMaterialWallEntity entity = new SurveyHousingMaterialWallEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingMaterialWallDto convertToDto(
      SurveyHousingMaterialWallEntity entity, Object... args) {
    SurveyHousingMaterialWallDto dto = new SurveyHousingMaterialWallDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
