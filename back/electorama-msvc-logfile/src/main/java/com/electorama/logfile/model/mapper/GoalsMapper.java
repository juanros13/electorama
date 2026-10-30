package com.electorama.logfile.model.mapper;

import com.electorama.logfile.model.dto.GoalsDto;
import com.electorama.logfile.model.entity.GoalsEntity;
import org.springframework.beans.BeanUtils;

public class GoalsMapper extends BaseMapper<GoalsEntity, GoalsDto> {

  @Override
  public GoalsEntity convertToEntity(GoalsDto dto, Object... args) {
    GoalsEntity calendarEntity = new GoalsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public GoalsDto convertToDto(GoalsEntity entity, Object... args) {
    GoalsDto calendar = new GoalsDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
