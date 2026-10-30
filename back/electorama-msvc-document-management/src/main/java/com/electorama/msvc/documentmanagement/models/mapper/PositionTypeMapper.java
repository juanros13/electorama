package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.PositionTypeDto;
import com.electorama.msvc.documentmanagement.models.entity.PositionTypeEntity;
import org.springframework.beans.BeanUtils;

public class PositionTypeMapper extends BaseMapper<PositionTypeEntity, PositionTypeDto> {

  @Override
  public PositionTypeEntity convertToEntity(PositionTypeDto dto, Object... args) {
    PositionTypeEntity entity = new PositionTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public PositionTypeDto convertToDto(PositionTypeEntity entity, Object... args) {
    PositionTypeDto dto = new PositionTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
