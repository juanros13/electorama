package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.SenderDto;
import com.electorama.msvc.documentmanagement.models.entity.SenderEntity;
import org.springframework.beans.BeanUtils;

public class SenderMapper extends BaseMapper<SenderEntity, SenderDto> {

  private final PositionPersonMapper positionPersonMapper = new PositionPersonMapper();

  @Override
  public SenderEntity convertToEntity(SenderDto dto, Object... args) {
    SenderEntity entity = new SenderEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    if (dto.getPositionPerson() != null) {
      entity.setPositionPerson(positionPersonMapper.convertToEntity(dto.getPositionPerson()));
    }
    return entity;
  }

  @Override
  public SenderDto convertToDto(SenderEntity entity, Object... args) {
    SenderDto dto = new SenderDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    if (entity.getPositionPerson() != null) {
      dto.setPositionPerson(positionPersonMapper.convertToDto(entity.getPositionPerson()));
    }
    return dto;
  }
}
