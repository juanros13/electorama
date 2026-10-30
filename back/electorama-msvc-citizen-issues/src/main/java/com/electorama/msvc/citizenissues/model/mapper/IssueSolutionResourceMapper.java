package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionResourceDto;
import com.electorama.msvc.citizenissues.model.entity.IssueSolutionResourceEntity;

public class IssueSolutionResourceMapper
    extends BaseMapper<IssueSolutionResourceEntity, IssueSolutionResourceDto> {

  private final IssueCategoryMapper issueCategoryMapper = new IssueCategoryMapper();

  @Override
  public IssueSolutionResourceEntity convertToEntity(IssueSolutionResourceDto dto, Object... args) {
    IssueSolutionResourceEntity entity = new IssueSolutionResourceEntity();
    if (dto != null) {
      entity.setId(dto.getId());
      entity.setName(dto.getName());
      entity.setIssueCategory(issueCategoryMapper.convertToEntity(dto.getIssueCategory()));
    }
    return entity;
  }

  @Override
  public IssueSolutionResourceDto convertToDto(IssueSolutionResourceEntity entity, Object... args) {
    IssueSolutionResourceDto dto = new IssueSolutionResourceDto();
    if (entity != null) {
      dto.setId(entity.getId());
      dto.setName(entity.getName());
      dto.setIssueCategory(issueCategoryMapper.convertToDto(entity.getIssueCategory()));
    }
    return dto;
  }
}
