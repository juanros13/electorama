package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothIssuesCategoryDto;
import com.electorama.elections.models.entity.PollingBoothIssuesCategoryEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothIssuesCategoryMapper
    extends BaseMapper<PollingBoothIssuesCategoryEntity, PollingBoothIssuesCategoryDto> {

  @Override
  public PollingBoothIssuesCategoryEntity convertToEntity(
      PollingBoothIssuesCategoryDto dto, Object... args) {
    PollingBoothIssuesCategoryEntity PollingBoothIssuesCategoryEntity =
        new PollingBoothIssuesCategoryEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, PollingBoothIssuesCategoryEntity);
    }
    return PollingBoothIssuesCategoryEntity;
  }

  @Override
  public PollingBoothIssuesCategoryDto convertToDto(
      PollingBoothIssuesCategoryEntity entity, Object... args) {
    PollingBoothIssuesCategoryDto PollingBoothIssuesCategoryDto =
        new PollingBoothIssuesCategoryDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, PollingBoothIssuesCategoryDto);
    }
    return PollingBoothIssuesCategoryDto;
  }
}
