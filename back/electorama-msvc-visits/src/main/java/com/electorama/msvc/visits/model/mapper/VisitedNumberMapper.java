package com.electorama.msvc.visits.model.mapper;

import com.electorama.msvc.visits.model.dto.VisitedNumberDto;
import com.electorama.msvc.visits.model.entity.VisitedNumberEntity;
import org.springframework.beans.BeanUtils;

public class VisitedNumberMapper extends BaseMapper<VisitedNumberEntity, VisitedNumberDto> {

  @Override
  public VisitedNumberEntity convertToEntity(VisitedNumberDto dto, Object... args) {
    VisitedNumberEntity calendarEntity = new VisitedNumberEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public VisitedNumberDto convertToDto(VisitedNumberEntity entity, Object... args) {
    VisitedNumberDto dto = new VisitedNumberDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
