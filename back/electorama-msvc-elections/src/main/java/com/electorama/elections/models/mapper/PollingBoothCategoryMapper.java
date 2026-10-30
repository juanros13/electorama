package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothCategoryDto;
import com.electorama.elections.models.entity.PollingBoothCategoryEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothCategoryMapper
    extends BaseMapper<PollingBoothCategoryEntity, PollingBoothCategoryDto> {

  @Override
  public PollingBoothCategoryEntity convertToEntity(PollingBoothCategoryDto dto, Object... args) {
    PollingBoothCategoryEntity pollingBoothCategoryEntity = new PollingBoothCategoryEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, pollingBoothCategoryEntity);
    }
    return pollingBoothCategoryEntity;
  }

  @Override
  public PollingBoothCategoryDto convertToDto(PollingBoothCategoryEntity entity, Object... args) {
    PollingBoothCategoryDto pollingBoothCategoryDto = new PollingBoothCategoryDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, pollingBoothCategoryDto);
    }
    return pollingBoothCategoryDto;
  }
}
