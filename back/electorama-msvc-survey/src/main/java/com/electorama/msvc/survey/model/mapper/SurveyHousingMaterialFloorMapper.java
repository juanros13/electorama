package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveyHousingMaterialFloorDto;
import com.electorama.msvc.survey.model.entity.SurveyHousingMaterialFloorEntity;
import org.springframework.beans.BeanUtils;

public class SurveyHousingMaterialFloorMapper
    extends BaseMapper<SurveyHousingMaterialFloorEntity, SurveyHousingMaterialFloorDto> {

  @Override
  public SurveyHousingMaterialFloorEntity convertToEntity(
      SurveyHousingMaterialFloorDto dto, Object... args) {
    SurveyHousingMaterialFloorEntity entity = new SurveyHousingMaterialFloorEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveyHousingMaterialFloorDto convertToDto(
      SurveyHousingMaterialFloorEntity entity, Object... args) {
    SurveyHousingMaterialFloorDto dto = new SurveyHousingMaterialFloorDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
