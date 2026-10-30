package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.FederalDistrict;
import com.electorama.geography.model.entity.FederalDistrictEntity;
import org.springframework.beans.BeanUtils;

public class FederalDistrictMapper extends BaseMapper<FederalDistrictEntity, FederalDistrict> {

  @Override
  public FederalDistrictEntity convertToEntity(FederalDistrict dto, Object... args) {
    FederalDistrictEntity calendarEntity = new FederalDistrictEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public FederalDistrict convertToDto(FederalDistrictEntity entity, Object... args) {
    FederalDistrict calendar = new FederalDistrict();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
