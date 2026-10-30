package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.CatMaritalStatusDto;
import com.electorama.msvc.followers.model.entity.CatMaritalStatusEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class CatMaritalStatusMapper
    extends BaseMapper<CatMaritalStatusEntity, CatMaritalStatusDto> {

  @Override
  public CatMaritalStatusEntity convertToEntity(CatMaritalStatusDto dto, Object... args) {
    CatMaritalStatusEntity calendarEntity = new CatMaritalStatusEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public CatMaritalStatusDto convertToDto(CatMaritalStatusEntity entity, Object... args) {
    CatMaritalStatusDto calendar = new CatMaritalStatusDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<CatMaritalStatusDto> convertToDtoOptional(
      CatMaritalStatusEntity entity, Object... args) {
    Optional<CatMaritalStatusDto> calendar = Optional.of(new CatMaritalStatusDto());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
