package com.electorama.elections.models.mapper;

import com.electorama.elections.models.dto.PollingBoothElectionAssociationDto;
import com.electorama.elections.models.entity.PollingBoothElectionAssociationEntity;
import org.springframework.beans.BeanUtils;

public class PollingBoothElectionAssociationMapper
    extends BaseMapper<PollingBoothElectionAssociationEntity, PollingBoothElectionAssociationDto> {

  @Override
  public PollingBoothElectionAssociationEntity convertToEntity(
      PollingBoothElectionAssociationDto dto, Object... args) {
    PollingBoothElectionAssociationEntity PollingBoothElectionAssociationEntity =
        new PollingBoothElectionAssociationEntity();
    if (dto != null) {

      BeanUtils.copyProperties(dto, PollingBoothElectionAssociationEntity);
    }
    return PollingBoothElectionAssociationEntity;
  }

  @Override
  public PollingBoothElectionAssociationDto convertToDto(
      PollingBoothElectionAssociationEntity entity, Object... args) {
    PollingBoothElectionAssociationDto PollingBoothElectionAssociationDto =
        new PollingBoothElectionAssociationDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, PollingBoothElectionAssociationDto);
    }
    return PollingBoothElectionAssociationDto;
  }
}
