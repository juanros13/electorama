package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.LocalDistrict;
import com.electorama.geography.model.entity.LocalDistrictEntity;
import org.springframework.beans.BeanUtils;

public class LocalDistrictMapper extends BaseMapper<LocalDistrictEntity, LocalDistrict> {

  @Override
  public LocalDistrictEntity convertToEntity(LocalDistrict dto, Object... args) {
    LocalDistrictEntity calendarEntity = new LocalDistrictEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public LocalDistrict convertToDto(LocalDistrictEntity entity, Object... args) {
    LocalDistrict calendar = new LocalDistrict();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
