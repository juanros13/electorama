package com.electorama.logfile.model.mapper;

import com.electorama.logfile.model.dto.InstrumentsDto;
import com.electorama.logfile.model.entity.InstrumentsEntity;
import org.springframework.beans.BeanUtils;

public class InstrumentsMapper extends BaseMapper<InstrumentsEntity, InstrumentsDto> {

  @Override
  public InstrumentsEntity convertToEntity(InstrumentsDto dto, Object... args) {
    InstrumentsEntity calendarEntity = new InstrumentsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public InstrumentsDto convertToDto(InstrumentsEntity entity, Object... args) {
    InstrumentsDto calendar = new InstrumentsDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
