package com.electorama.msvc.survey.model.mapper;

import com.electorama.msvc.survey.model.dto.SurveySectionGoalDto;
import com.electorama.msvc.survey.model.entity.SurveySectionGoalEntity;
import org.springframework.beans.BeanUtils;

public class SurveySectionGoalMapper
    extends BaseMapper<SurveySectionGoalEntity, SurveySectionGoalDto> {

  @Override
  public SurveySectionGoalEntity convertToEntity(SurveySectionGoalDto dto, Object... args) {
    SurveySectionGoalEntity entity = new SurveySectionGoalEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public SurveySectionGoalDto convertToDto(SurveySectionGoalEntity entity, Object... args) {
    SurveySectionGoalDto dto = new SurveySectionGoalDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
