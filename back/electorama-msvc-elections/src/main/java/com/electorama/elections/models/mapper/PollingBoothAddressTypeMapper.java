package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothAddressTypeDto;
import com.electorama.elections.models.entity.PollingBoothAddressTypeEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothAddressTypeMapper
    extends BaseMapper<PollingBoothAddressTypeEntity, PollingBoothAddressTypeDto> {

  @Override
  public PollingBoothAddressTypeEntity convertToEntity(
      PollingBoothAddressTypeDto dto, Object... args) {
    PollingBoothAddressTypeEntity pollingBoothAddressTypeEntity =
        new PollingBoothAddressTypeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, pollingBoothAddressTypeEntity);
    }
    return pollingBoothAddressTypeEntity;
  }

  @Override
  public PollingBoothAddressTypeDto convertToDto(
      PollingBoothAddressTypeEntity entity, Object... args) {
    PollingBoothAddressTypeDto pollingBoothAddressTypeDto = new PollingBoothAddressTypeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, pollingBoothAddressTypeDto);
    }
    return pollingBoothAddressTypeDto;
  }
}
