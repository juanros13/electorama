package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueStatusDto;
import com.electorama.msvc.citizenissues.model.entity.IssueStatusEntity;
import org.springframework.beans.BeanUtils;

public class IssueStatusMapper extends BaseMapper<IssueStatusEntity, IssueStatusDto> {
  @Override
  public IssueStatusEntity convertToEntity(IssueStatusDto dto, Object... args) {
    IssueStatusEntity entity = new IssueStatusEntity();
    if (dto != null) BeanUtils.copyProperties(dto, entity);

    return entity;
  }

  @Override
  public IssueStatusDto convertToDto(IssueStatusEntity entity, Object... args) {
    IssueStatusDto dto = new IssueStatusDto();
    if (entity != null) BeanUtils.copyProperties(entity, dto);

    return dto;
  }
}
