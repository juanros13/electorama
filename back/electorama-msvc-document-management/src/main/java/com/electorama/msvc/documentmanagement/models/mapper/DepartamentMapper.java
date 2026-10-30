package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.DepartamentDto;
import com.electorama.msvc.documentmanagement.models.entity.DepartamentEntity;
import org.springframework.beans.BeanUtils;

public class DepartamentMapper extends BaseMapper<DepartamentEntity, DepartamentDto> {

  @Override
  public DepartamentEntity convertToEntity(DepartamentDto dto, Object... args) {
    DepartamentEntity entity = new DepartamentEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }

    return entity;
  }

  @Override
  public DepartamentDto convertToDto(DepartamentEntity entity, Object... args) {
    DepartamentDto dto = new DepartamentDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
