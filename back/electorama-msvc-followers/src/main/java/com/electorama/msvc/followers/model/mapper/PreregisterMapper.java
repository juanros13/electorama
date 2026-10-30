package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.PreregisterDto;
import com.electorama.msvc.followers.model.entity.PreregisterEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface PreregisterMapper {
  PreregisterMapper INSTANCE = Mappers.getMapper(PreregisterMapper.class);

  PreregisterDto toDto(PreregisterEntity entity);

  PreregisterEntity toEntity(PreregisterDto dto);
}
