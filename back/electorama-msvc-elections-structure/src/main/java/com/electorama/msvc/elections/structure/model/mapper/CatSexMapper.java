package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.CatSexDto;
import com.electorama.msvc.elections.structure.model.entity.CatSexEntity;
import org.springframework.beans.BeanUtils;

public class CatSexMapper extends BaseMapper<CatSexEntity, CatSexDto> {

  @Override
  public CatSexEntity convertToEntity(CatSexDto dto, Object... args) {
    CatSexEntity entity = new CatSexEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public CatSexDto convertToDto(CatSexEntity entity, Object... args) {
    CatSexDto dto = new CatSexDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
