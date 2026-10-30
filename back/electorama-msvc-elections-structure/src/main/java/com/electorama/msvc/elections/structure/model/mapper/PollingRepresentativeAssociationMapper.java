package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeAssociationDto;
import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import org.springframework.beans.BeanUtils;

public class PollingRepresentativeAssociationMapper {

  private final CatSexMapper catSexMapper = new CatSexMapper();

  private final PollingRepresentativeTypeMapper pollingRepresentativeTypeMapper =
      new PollingRepresentativeTypeMapper();

  public PollingRepresentativeAssociationDto convertToPollingRepresentativeAssociation(
      PollingRepresentativeDto dto, Object... args) {
    PollingRepresentativeAssociationDto entity = new PollingRepresentativeAssociationDto();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  public PollingRepresentativeDto convertToPollingRepresentativeDto(
      PollingRepresentativeAssociationDto entity, Object... args) {
    PollingRepresentativeDto dto = new PollingRepresentativeDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }

    return dto;
  }
}
