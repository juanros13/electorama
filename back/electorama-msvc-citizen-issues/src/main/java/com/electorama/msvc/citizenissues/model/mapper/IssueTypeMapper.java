package com.electorama.msvc.citizenissues.model.mapper;

import com.electorama.msvc.citizenissues.model.dto.IssueTypeDto;
import com.electorama.msvc.citizenissues.model.entity.IssueTypeEntity;
import org.springframework.beans.BeanUtils;

public class IssueTypeMapper extends BaseMapper<IssueTypeEntity, IssueTypeDto> {

  @Override
  public IssueTypeEntity convertToEntity(IssueTypeDto dto, Object... args) {
    IssueTypeEntity entity = new IssueTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }

    return entity;
  }

  @Override
  public IssueTypeDto convertToDto(IssueTypeEntity entity, Object... args) {
    IssueTypeDto dto = new IssueTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
