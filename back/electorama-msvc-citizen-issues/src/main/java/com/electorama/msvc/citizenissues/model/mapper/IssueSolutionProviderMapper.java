package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionProviderDto;
import com.electorama.msvc.citizenissues.model.entity.IssueSolutionProviderEntity;
import org.springframework.beans.BeanUtils;

public class IssueSolutionProviderMapper
    extends BaseMapper<IssueSolutionProviderEntity, IssueSolutionProviderDto> {
  @Override
  public IssueSolutionProviderEntity convertToEntity(IssueSolutionProviderDto dto, Object... args) {
    IssueSolutionProviderEntity entity = new IssueSolutionProviderEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      entity.setId(dto.getId());
      entity.setName(dto.getName());
    }
    return entity;
  }

  @Override
  public IssueSolutionProviderDto convertToDto(IssueSolutionProviderEntity entity, Object... args) {
    IssueSolutionProviderDto dto = new IssueSolutionProviderDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setId(entity.getId());
      dto.setName(entity.getName());
    }
    return dto;
  }
}
