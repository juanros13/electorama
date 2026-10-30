package com.electorama.msvc.electionsresults.models.mapper;

import com.electorama.msvc.electionsresults.models.dto.ElectionResultDto;
import com.electorama.msvc.electionsresults.models.entity.ElectionResultEntity;
import org.springframework.beans.BeanUtils;

public class ElectionResultMapper extends BaseMapper<ElectionResultEntity, ElectionResultDto> {

  @Override
  public ElectionResultEntity convertToEntity(ElectionResultDto dto, Object... args) {
    ElectionResultEntity electionsEntity = new ElectionResultEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public ElectionResultDto convertToDto(ElectionResultEntity entity, Object... args) {
    ElectionResultDto ElectionResultDto = new ElectionResultDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, ElectionResultDto);
    }
    return ElectionResultDto;
  }
}
