package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.ElectionPositionDto;
import com.electorama.elections.models.entity.ElectionPositionEntity;
import org.springframework.beans.BeanUtils;

public class ElectionPositionMapper
    extends BaseMapper<ElectionPositionEntity, ElectionPositionDto> {

  @Override
  public ElectionPositionEntity convertToEntity(ElectionPositionDto dto, Object... args) {
    ElectionPositionEntity electionsEntity = new ElectionPositionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public ElectionPositionDto convertToDto(ElectionPositionEntity entity, Object... args) {
    ElectionPositionDto calendar = new ElectionPositionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
