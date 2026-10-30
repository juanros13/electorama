package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeTypeDto;
import com.electorama.msvc.elections.structure.model.entity.PollingRepresentativeTypeEntity;
import org.springframework.beans.BeanUtils;

public class PollingRepresentativeTypeMapper
    extends BaseMapper<PollingRepresentativeTypeEntity, PollingRepresentativeTypeDto> {

  @Override
  public PollingRepresentativeTypeEntity convertToEntity(
      PollingRepresentativeTypeDto dto, Object... args) {
    PollingRepresentativeTypeEntity entity = new PollingRepresentativeTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public PollingRepresentativeTypeDto convertToDto(
      PollingRepresentativeTypeEntity entity, Object... args) {
    PollingRepresentativeTypeDto dto = new PollingRepresentativeTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
