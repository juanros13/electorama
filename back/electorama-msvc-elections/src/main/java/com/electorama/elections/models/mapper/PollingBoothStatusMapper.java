package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothStatusDto;
import com.electorama.elections.models.entity.PollingBoothStatusEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothStatusMapper
    extends BaseMapper<PollingBoothStatusEntity, PollingBoothStatusDto> {

  @Override
  public PollingBoothStatusEntity convertToEntity(PollingBoothStatusDto dto, Object... args) {
    PollingBoothStatusEntity pollingBoothStatusEntity = new PollingBoothStatusEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, pollingBoothStatusEntity);
    }
    return pollingBoothStatusEntity;
  }

  @Override
  public PollingBoothStatusDto convertToDto(PollingBoothStatusEntity entity, Object... args) {
    PollingBoothStatusDto pollingBoothStatusDto = new PollingBoothStatusDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, pollingBoothStatusDto);
    }
    return pollingBoothStatusDto;
  }
}
