package com.electorama.logfile.model.mapper;

import com.electorama.logfile.model.dto.IndicatorsDto;
import com.electorama.logfile.model.entity.IndicatorsEntity;
import org.springframework.beans.BeanUtils;

public class IndicatorsMapper extends BaseMapper<IndicatorsEntity, IndicatorsDto> {

  @Override
  public IndicatorsEntity convertToEntity(IndicatorsDto dto, Object... args) {
    IndicatorsEntity calendarEntity = new IndicatorsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public IndicatorsDto convertToDto(IndicatorsEntity entity, Object... args) {
    IndicatorsDto calendar = new IndicatorsDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
