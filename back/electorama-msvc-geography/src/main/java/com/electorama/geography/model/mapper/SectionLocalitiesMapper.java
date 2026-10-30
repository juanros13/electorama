package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.SectionLocalities;
import com.electorama.geography.model.entity.SectionLocalitiesEntity;
import org.springframework.beans.BeanUtils;

public class SectionLocalitiesMapper
    extends BaseMapper<SectionLocalitiesEntity, SectionLocalities> {

  @Override
  public SectionLocalitiesEntity convertToEntity(SectionLocalities dto, Object... args) {
    SectionLocalitiesEntity calendarEntity = new SectionLocalitiesEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public SectionLocalities convertToDto(SectionLocalitiesEntity entity, Object... args) {
    SectionLocalities calendar = new SectionLocalities();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
