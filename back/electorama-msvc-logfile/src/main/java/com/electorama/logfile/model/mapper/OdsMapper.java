package com.electorama.logfile.model.mapper;

import com.electorama.logfile.model.dto.OdsDto;
import com.electorama.logfile.model.entity.OdsEntity;
import org.springframework.beans.BeanUtils;

public class OdsMapper extends BaseMapper<OdsEntity, OdsDto> {

  @Override
  public OdsEntity convertToEntity(OdsDto dto, Object... args) {
    OdsEntity calendarEntity = new OdsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public OdsDto convertToDto(OdsEntity entity, Object... args) {
    OdsDto calendar = new OdsDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
