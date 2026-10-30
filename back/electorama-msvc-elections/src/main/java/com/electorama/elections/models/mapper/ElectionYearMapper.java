package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.ElectionYearDto;
import com.electorama.elections.models.entity.ElectionYearEntity;
import org.springframework.beans.BeanUtils;

public class ElectionYearMapper extends BaseMapper<ElectionYearEntity, ElectionYearDto> {

  @Override
  public ElectionYearEntity convertToEntity(ElectionYearDto dto, Object... args) {
    ElectionYearEntity electionsEntity = new ElectionYearEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public ElectionYearDto convertToDto(ElectionYearEntity entity, Object... args) {
    ElectionYearDto calendar = new ElectionYearDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
