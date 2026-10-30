package com.electorama.msvc.visits.model.mapper;

import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.model.entity.VisitedPeopleEntity;
import org.springframework.beans.BeanUtils;

public class VisitedPeopleMapper extends BaseMapper<VisitedPeopleEntity, VisitedPeopleDto> {

  private final VisitedMapperExtra VisitedMapperExtra = new VisitedMapperExtra();

  @Override
  public VisitedPeopleEntity convertToEntity(VisitedPeopleDto dto, Object... args) {
    VisitedPeopleEntity calendarEntity = new VisitedPeopleEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
      calendarEntity.setVisitedEntity(VisitedMapperExtra.convertToEntity(dto.getVisitedEntity()));
    }
    return calendarEntity;
  }

  @Override
  public VisitedPeopleDto convertToDto(VisitedPeopleEntity entity, Object... args) {
    VisitedPeopleDto dto = new VisitedPeopleDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setVisitedEntity(VisitedMapperExtra.convertToDto(entity.getVisitedEntity()));
    }
    return dto;
  }
}
