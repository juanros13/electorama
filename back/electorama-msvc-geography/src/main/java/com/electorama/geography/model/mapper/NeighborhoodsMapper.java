package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.Neighborhoods;
import com.electorama.geography.model.entity.NeighborhoodsEntity;
import org.springframework.beans.BeanUtils;

public class NeighborhoodsMapper extends BaseMapper<NeighborhoodsEntity, Neighborhoods> {

  @Override
  public NeighborhoodsEntity convertToEntity(Neighborhoods dto, Object... args) {
    NeighborhoodsEntity calendarEntity = new NeighborhoodsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public Neighborhoods convertToDto(NeighborhoodsEntity entity, Object... args) {
    Neighborhoods calendar = new Neighborhoods();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
