package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.ElectionTypeDto;
import com.electorama.elections.models.entity.ElectionTypeEntity;
import org.springframework.beans.BeanUtils;

public class ElectionTypeMapper extends BaseMapper<ElectionTypeEntity, ElectionTypeDto> {

  @Override
  public ElectionTypeEntity convertToEntity(ElectionTypeDto dto, Object... args) {
    ElectionTypeEntity electionsEntity = new ElectionTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public ElectionTypeDto convertToDto(ElectionTypeEntity entity, Object... args) {
    ElectionTypeDto electionDto = new ElectionTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, electionDto);
    }
    return electionDto;
  }
}
