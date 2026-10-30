package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothTypeDto;
import com.electorama.elections.models.entity.PollingBoothTypeEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothTypeMapper
    extends BaseMapper<PollingBoothTypeEntity, PollingBoothTypeDto> {

  @Override
  public PollingBoothTypeEntity convertToEntity(PollingBoothTypeDto dto, Object... args) {
    PollingBoothTypeEntity pollingBoothTypeEntity = new PollingBoothTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, pollingBoothTypeEntity);
    }
    return pollingBoothTypeEntity;
  }

  @Override
  public PollingBoothTypeDto convertToDto(PollingBoothTypeEntity entity, Object... args) {
    PollingBoothTypeDto pollingBoothTypeDto = new PollingBoothTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, pollingBoothTypeDto);
    }
    return pollingBoothTypeDto;
  }
}
