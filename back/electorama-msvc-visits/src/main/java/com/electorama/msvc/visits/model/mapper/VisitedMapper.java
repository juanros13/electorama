package com.electorama.msvc.visits.model.mapper;

import com.electorama.msvc.visits.model.dto.VisitedDto;
import com.electorama.msvc.visits.model.entity.VisitedEntity;
import org.springframework.beans.BeanUtils;

public class VisitedMapper extends BaseMapper<VisitedEntity, VisitedDto> {

  private final VisitedPeopleMapper visitedPeopleMapper = new VisitedPeopleMapper();

  @Override
  public VisitedEntity convertToEntity(VisitedDto dto, Object... args) {
    VisitedEntity entity = new VisitedEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      entity.setVisitedPeople(visitedPeopleMapper.convertToEntityList(dto.getVisitedPeople()));
    }
    return entity;
  }

  @Override
  public VisitedDto convertToDto(VisitedEntity entity, Object... args) {
    VisitedDto dto = new VisitedDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setVisitedPeople(visitedPeopleMapper.convertToDtoList(entity.getVisitedPeople()));
    }
    return dto;
  }
}
