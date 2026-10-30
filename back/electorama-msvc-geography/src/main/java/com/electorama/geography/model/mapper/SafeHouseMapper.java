package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.SafeHouse;
import com.electorama.geography.model.entity.SafeHouseEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class SafeHouseMapper extends BaseMapper<SafeHouseEntity, SafeHouse> {

  @Override
  public SafeHouseEntity convertToEntity(SafeHouse dto, Object... args) {
    SafeHouseEntity calendarEntity = new SafeHouseEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public SafeHouse convertToDto(SafeHouseEntity entity, Object... args) {
    SafeHouse calendar = new SafeHouse();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<SafeHouse> convertToDtoOptional(
      Optional<SafeHouseEntity> entityOptional, Object... args) {
    if (entityOptional.isPresent()) {
      SafeHouseEntity entity = entityOptional.get();
      SafeHouse safeHouse = new SafeHouse();
      BeanUtils.copyProperties(entity, safeHouse);
      return Optional.of(safeHouse);
    } else {
      return Optional.empty();
    }
  }
}
