package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionDocumentsCategoryDto;
import com.electorama.msvc.citizenissues.model.entity.IssueSolutionDocumentsCategoryEntity;
import org.springframework.beans.BeanUtils;

public class IssueSolutionDocumentsCategoryMapper
    extends BaseMapper<IssueSolutionDocumentsCategoryEntity, IssueSolutionDocumentsCategoryDto> {
  @Override
  public IssueSolutionDocumentsCategoryEntity convertToEntity(
      IssueSolutionDocumentsCategoryDto dto, Object... args) {
    IssueSolutionDocumentsCategoryEntity entity = new IssueSolutionDocumentsCategoryEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      entity.setId(dto.getId());
      entity.setName(dto.getName());
    }
    return entity;
  }

  @Override
  public IssueSolutionDocumentsCategoryDto convertToDto(
      IssueSolutionDocumentsCategoryEntity entity, Object... args) {
    IssueSolutionDocumentsCategoryDto dto = new IssueSolutionDocumentsCategoryDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setId(entity.getId());
      dto.setName(entity.getName());
    }
    return dto;
  }
}
