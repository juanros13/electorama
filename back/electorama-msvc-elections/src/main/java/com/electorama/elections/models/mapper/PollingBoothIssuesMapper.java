package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothIssuesDto;
import com.electorama.elections.models.entity.PollingBoothIssuesEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothIssuesMapper
    extends BaseMapper<PollingBoothIssuesEntity, PollingBoothIssuesDto> {

  private final PollingBoothIssuesSubCategoryMapper pollingBoothIssuesSubCategoryMapper =
      new PollingBoothIssuesSubCategoryMapper();

  @Override
  public PollingBoothIssuesEntity convertToEntity(PollingBoothIssuesDto dto, Object... args) {
    PollingBoothIssuesEntity PollingBoothIssuesEntity = new PollingBoothIssuesEntity();
    if (dto != null) {

      BeanUtils.copyProperties(dto, PollingBoothIssuesEntity);
      PollingBoothIssuesEntity.setPollingBoothIssuesSubCategory(
          pollingBoothIssuesSubCategoryMapper.convertToEntity(
              dto.getPollingBoothIssuesSubCategory()));
    }
    return PollingBoothIssuesEntity;
  }

  @Override
  public PollingBoothIssuesDto convertToDto(PollingBoothIssuesEntity entity, Object... args) {
    PollingBoothIssuesDto PollingBoothIssuesDto = new PollingBoothIssuesDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, PollingBoothIssuesDto);
      PollingBoothIssuesDto.setPollingBoothIssuesSubCategory(
          pollingBoothIssuesSubCategoryMapper.convertToDto(
              entity.getPollingBoothIssuesSubCategory()));
    }
    return PollingBoothIssuesDto;
  }
}
