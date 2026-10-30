package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.PositionPersonDto;
import com.electorama.msvc.documentmanagement.models.entity.PositionPersonEntity;
import org.springframework.beans.BeanUtils;

public class PositionPersonMapper extends BaseMapper<PositionPersonEntity, PositionPersonDto> {

  private final PositionMapper positionMapper = new PositionMapper();

  private final PersonMapper personMapper = new PersonMapper();

  @Override
  public PositionPersonEntity convertToEntity(PositionPersonDto dto, Object... args) {
    PositionPersonEntity entity = new PositionPersonEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    if (dto.getPosition() != null) {
      entity.setPosition(positionMapper.convertToEntity(dto.getPosition()));
    }
    if (dto.getPerson() != null) {
      entity.setPerson(personMapper.convertToEntity(dto.getPerson()));
    }
    return entity;
  }

  @Override
  public PositionPersonDto convertToDto(PositionPersonEntity entity, Object... args) {
    PositionPersonDto dto = new PositionPersonDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    if (entity.getPosition() != null) {
      dto.setPosition(positionMapper.convertToDto(entity.getPosition()));
    }
    if (entity.getPerson() != null) {
      dto.setPerson(personMapper.convertToDto(entity.getPerson()));
    }
    return dto;
  }
}
