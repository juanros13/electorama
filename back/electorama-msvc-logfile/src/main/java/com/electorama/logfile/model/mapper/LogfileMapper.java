package com.electorama.logfile.model.mapper;

import com.electorama.logfile.model.dto.LogfileDto;
import com.electorama.logfile.model.entity.LogfileEntity;
import org.springframework.beans.BeanUtils;

public class LogfileMapper extends BaseMapper<LogfileEntity, LogfileDto> {

  @Override
  public LogfileEntity convertToEntity(LogfileDto dto, Object... args) {
    LogfileEntity calendarEntity = new LogfileEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public LogfileDto convertToDto(LogfileEntity entity, Object... args) {
    LogfileDto calendar = new LogfileDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
