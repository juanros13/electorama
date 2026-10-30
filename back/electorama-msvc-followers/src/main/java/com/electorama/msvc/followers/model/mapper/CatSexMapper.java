package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.CatSexDto;
import com.electorama.msvc.followers.model.entity.CatSexEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class CatSexMapper extends BaseMapper<CatSexEntity, CatSexDto> {

  @Override
  public CatSexEntity convertToEntity(CatSexDto dto, Object... args) {
    CatSexEntity calendarEntity = new CatSexEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public CatSexDto convertToDto(CatSexEntity entity, Object... args) {
    CatSexDto calendar = new CatSexDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<CatSexDto> convertToDtoOptional(CatSexEntity entity, Object... args) {
    Optional<CatSexDto> calendar = Optional.of(new CatSexDto());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
