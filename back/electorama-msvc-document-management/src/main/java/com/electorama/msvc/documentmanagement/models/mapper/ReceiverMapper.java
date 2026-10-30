package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.ReceiverDto;
import com.electorama.msvc.documentmanagement.models.entity.ReceiverEntity;
import org.springframework.beans.BeanUtils;

public class ReceiverMapper extends BaseMapper<ReceiverEntity, ReceiverDto> {

  private final PositionPersonMapper positionPersonMapper = new PositionPersonMapper();

  @Override
  public ReceiverEntity convertToEntity(ReceiverDto dto, Object... args) {
    ReceiverEntity entity = new ReceiverEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    if (dto.getPositionPerson() != null) {
      entity.setPositionPerson(positionPersonMapper.convertToEntity(dto.getPositionPerson()));
    }
    return entity;
  }

  @Override
  public ReceiverDto convertToDto(ReceiverEntity entity, Object... args) {
    ReceiverDto dto = new ReceiverDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    if (entity.getPositionPerson() != null) {
      dto.setPositionPerson(positionPersonMapper.convertToDto(entity.getPositionPerson()));
    }
    return dto;
  }
}
