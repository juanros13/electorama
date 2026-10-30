package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingMaterialRoofDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingMaterialRoofEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingMaterialRoofMapper
    extends BaseMapper<SurveyHousingMaterialRoofEntity, SurveyHousingMaterialRoofDto> {

  @Override
  public SurveyHousingMaterialRoofEntity convertToEntity(
      SurveyHousingMaterialRoofDto dto, Object... args) {
    SurveyHousingMaterialRoofEntity entity = new SurveyHousingMaterialRoofEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingMaterialRoofDto convertToDto(
      SurveyHousingMaterialRoofEntity entity, Object... args) {
    SurveyHousingMaterialRoofDto dto = new SurveyHousingMaterialRoofDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
