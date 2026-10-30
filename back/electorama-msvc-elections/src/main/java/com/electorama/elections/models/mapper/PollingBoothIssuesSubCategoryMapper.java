package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothIssuesSubCategoryDto;
import com.electorama.elections.models.entity.PollingBoothIssuesSubCategoryEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothIssuesSubCategoryMapper
    extends BaseMapper<PollingBoothIssuesSubCategoryEntity, PollingBoothIssuesSubCategoryDto> {

  private final PollingBoothIssuesCategoryMapper pollingBoothIssuesCategoryMapper =
      new PollingBoothIssuesCategoryMapper();

  @Override
  public PollingBoothIssuesSubCategoryEntity convertToEntity(
      PollingBoothIssuesSubCategoryDto dto, Object... args) {
    PollingBoothIssuesSubCategoryEntity PollingBoothIssuesSubCategoryEntity =
        new PollingBoothIssuesSubCategoryEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, PollingBoothIssuesSubCategoryEntity);
      PollingBoothIssuesSubCategoryEntity.setPollingBoothIssuesCategory(
          pollingBoothIssuesCategoryMapper.convertToEntity(dto.getPollingBoothIssuesCategory()));
    }
    return PollingBoothIssuesSubCategoryEntity;
  }

  @Override
  public PollingBoothIssuesSubCategoryDto convertToDto(
      PollingBoothIssuesSubCategoryEntity entity, Object... args) {
    PollingBoothIssuesSubCategoryDto PollingBoothIssuesSubCategoryDto =
        new PollingBoothIssuesSubCategoryDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, PollingBoothIssuesSubCategoryDto);
      PollingBoothIssuesSubCategoryDto.setPollingBoothIssuesCategory(
          pollingBoothIssuesCategoryMapper.convertToDto(entity.getPollingBoothIssuesCategory()));
    }
    return PollingBoothIssuesSubCategoryDto;
  }
}
