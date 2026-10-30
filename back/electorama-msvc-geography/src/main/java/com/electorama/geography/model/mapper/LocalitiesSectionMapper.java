package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.LocalitiesSection;
import com.electorama.geography.model.entity.LocalitiesSectionEntity;
import org.springframework.beans.BeanUtils;

public class LocalitiesSectionMapper
    extends BaseMapper<LocalitiesSectionEntity, LocalitiesSection> {

  @Override
  public LocalitiesSectionEntity convertToEntity(LocalitiesSection dto, Object... args) {
    LocalitiesSectionEntity calendarEntity = new LocalitiesSectionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public LocalitiesSection convertToDto(LocalitiesSectionEntity entity, Object... args) {
    LocalitiesSection calendar = new LocalitiesSection();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
