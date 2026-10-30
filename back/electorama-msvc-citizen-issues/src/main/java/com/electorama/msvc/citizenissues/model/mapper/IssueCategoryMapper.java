package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueCategoryDto;
import com.electorama.msvc.citizenissues.model.entity.IssueCategoryEntity;
import org.springframework.beans.BeanUtils;

public class IssueCategoryMapper extends BaseMapper<IssueCategoryEntity, IssueCategoryDto> {

  @Override
  public IssueCategoryEntity convertToEntity(IssueCategoryDto dto, Object... args) {
    IssueCategoryEntity entity = new IssueCategoryEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }

    return entity;
  }

  @Override
  public IssueCategoryDto convertToDto(IssueCategoryEntity entity, Object... args) {
    IssueCategoryDto dto = new IssueCategoryDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
