package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import com.electorama.msvc.elections.structure.model.entity.PollingRepresentativeEntity;
import org.springframework.beans.BeanUtils;

public class PollingRepresentativeMapper
    extends BaseMapper<PollingRepresentativeEntity, PollingRepresentativeDto> {

  private final CatSexMapper catSexMapper = new CatSexMapper();

  private final PollingRepresentativeTypeMapper pollingRepresentativeTypeMapper =
      new PollingRepresentativeTypeMapper();

  @Override
  public PollingRepresentativeEntity convertToEntity(PollingRepresentativeDto dto, Object... args) {
    PollingRepresentativeEntity entity = new PollingRepresentativeEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getCatSex() != null) {
        entity.setCatSex(catSexMapper.convertToEntity(dto.getCatSex()));
      }
      if (dto.getPollingRepresentativeType() != null) {
        entity.setPollingRepresentativeType(
            pollingRepresentativeTypeMapper.convertToEntity(dto.getPollingRepresentativeType()));
      }
    }
    return entity;
  }

  @Override
  public PollingRepresentativeDto convertToDto(PollingRepresentativeEntity entity, Object... args) {
    PollingRepresentativeDto dto = new PollingRepresentativeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getCatSex() != null) {
        dto.setCatSex(catSexMapper.convertToDto(entity.getCatSex()));
      }
      if (entity.getPollingRepresentativeType() != null) {
        dto.setPollingRepresentativeType(
            pollingRepresentativeTypeMapper.convertToDto(entity.getPollingRepresentativeType()));
      }
    }

    return dto;
  }
}
