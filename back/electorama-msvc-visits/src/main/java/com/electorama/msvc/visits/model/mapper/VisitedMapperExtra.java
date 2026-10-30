package com.electorama.msvc.visits.model.mapper;

import com.electorama.msvc.visits.model.dto.VisitedDto;
import com.electorama.msvc.visits.model.entity.VisitedEntity;
import org.springframework.beans.BeanUtils;

public class VisitedMapperExtra extends BaseMapper<VisitedEntity, VisitedDto> {

  @Override
  public VisitedEntity convertToEntity(VisitedDto dto, Object... args) {
    VisitedEntity entity = new VisitedEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public VisitedDto convertToDto(VisitedEntity entity, Object... args) {
    VisitedDto dto = new VisitedDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
