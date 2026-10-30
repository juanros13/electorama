package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.PositionDto;
import com.electorama.msvc.documentmanagement.models.entity.PositionEntity;
import org.springframework.beans.BeanUtils;

public class PositionMapper extends BaseMapper<PositionEntity, PositionDto> {

  private final PositionTypeMapper positionTypeMapper = new PositionTypeMapper();

  private final DepartamentMapper departamentMapper = new DepartamentMapper();

  @Override
  public PositionEntity convertToEntity(PositionDto dto, Object... args) {
    PositionEntity entity = new PositionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    if (dto.getPositionType() != null) {
      entity.setPositionType(positionTypeMapper.convertToEntity(dto.getPositionType()));
    }
    if (dto.getDepartament() != null) {
      entity.setDepartament(departamentMapper.convertToEntity(dto.getDepartament()));
    }
    return entity;
  }

  @Override
  public PositionDto convertToDto(PositionEntity entity, Object... args) {
    PositionDto dto = new PositionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    if (entity.getPositionType() != null) {
      dto.setPositionType(positionTypeMapper.convertToDto(entity.getPositionType()));
    }
    if (entity.getDepartament() != null) {
      dto.setDepartament(departamentMapper.convertToDto(entity.getDepartament()));
    }
    return dto;
  }
}
