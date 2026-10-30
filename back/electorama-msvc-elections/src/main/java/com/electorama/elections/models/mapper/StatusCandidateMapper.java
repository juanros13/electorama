package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.StatusValidateDto;
import com.electorama.elections.models.entity.StatusValidateEntity;
import org.springframework.beans.BeanUtils;

public class StatusCandidateMapper extends BaseMapper<StatusValidateEntity, StatusValidateDto> {

  private final ElectionMapper electionMapper = new ElectionMapper();

  @Override
  public StatusValidateEntity convertToEntity(StatusValidateDto dto, Object... args) {
    StatusValidateEntity entity = new StatusValidateEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public StatusValidateDto convertToDto(StatusValidateEntity entity, Object... args) {
    StatusValidateDto dto = new StatusValidateDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
