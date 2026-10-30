package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionDto;
import com.electorama.msvc.citizenissues.model.entity.IssueSolutionEntity;
import org.springframework.beans.BeanUtils;

public class IssueSolutionMapper extends BaseMapper<IssueSolutionEntity, IssueSolutionDto> {

  private final IssueSolutionProviderMapper issueSolutionProviderMapper =
      new IssueSolutionProviderMapper();
  private final IssueSolutionResourceMapper issueSolutionResourceMapper =
      new IssueSolutionResourceMapper();

  @Override
  public IssueSolutionEntity convertToEntity(IssueSolutionDto dto, Object... args) {
    IssueSolutionEntity entity = new IssueSolutionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getId() != null) entity.setId(dto.getId());
      entity.setQuantity(dto.getQuantity());
      entity.setFollowerId(dto.getFollowerId());
      entity.setSolutionDate(dto.getSolutionDate());
      entity.setComments(dto.getComments());
      entity.setIssueSolutionProvider(
          issueSolutionProviderMapper.convertToEntity(dto.getIssueSolutionProvider()));
      entity.setIssueSolutionResource(
          issueSolutionResourceMapper.convertToEntity(dto.getIssueSolutionResource()));
    }
    return entity;
  }

  @Override
  public IssueSolutionDto convertToDto(IssueSolutionEntity entity, Object... args) {
    IssueSolutionDto dto = new IssueSolutionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getId() != null) dto.setId(entity.getId());
      dto.setQuantity(entity.getQuantity());
      dto.setFollowerId(entity.getFollowerId());
      dto.setSolutionDate(entity.getSolutionDate());
      dto.setComments(entity.getComments());
      dto.setIssueSolutionProvider(
          issueSolutionProviderMapper.convertToDto(entity.getIssueSolutionProvider()));
      dto.setIssueSolutionResource(
          issueSolutionResourceMapper.convertToDto(entity.getIssueSolutionResource()));
    }
    return dto;
  }
}
