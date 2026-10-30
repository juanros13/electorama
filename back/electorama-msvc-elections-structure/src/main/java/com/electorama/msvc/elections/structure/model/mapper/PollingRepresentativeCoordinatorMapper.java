package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeCoordinatorDto;
import com.electorama.msvc.elections.structure.model.entity.PollingRepresentativeCoordinatorEntity;
import org.springframework.beans.BeanUtils;

public class PollingRepresentativeCoordinatorMapper
    extends BaseMapper<
        PollingRepresentativeCoordinatorEntity, PollingRepresentativeCoordinatorDto> {

  private final CatSexMapper catSexMapper = new CatSexMapper();

  private final PollingRepresentativeTypeMapper pollingRepresentativeTypeMapper =
      new PollingRepresentativeTypeMapper();

  @Override
  public PollingRepresentativeCoordinatorEntity convertToEntity(
      PollingRepresentativeCoordinatorDto dto, Object... args) {
    PollingRepresentativeCoordinatorEntity entity = new PollingRepresentativeCoordinatorEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public PollingRepresentativeCoordinatorDto convertToDto(
      PollingRepresentativeCoordinatorEntity entity, Object... args) {
    PollingRepresentativeCoordinatorDto dto = new PollingRepresentativeCoordinatorDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }

    return dto;
  }
}
