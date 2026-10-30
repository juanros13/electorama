package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.Municipality;
import com.electorama.geography.model.entity.MunicipalityEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class MunicipalityMapper extends BaseMapper<MunicipalityEntity, Municipality> {

  @Override
  public MunicipalityEntity convertToEntity(Municipality dto, Object... args) {
    MunicipalityEntity calendarEntity = new MunicipalityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public Municipality convertToDto(MunicipalityEntity entity, Object... args) {
    Municipality calendar = new Municipality();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<Municipality> convertToDtoOptional(MunicipalityEntity entity, Object... args) {
    Optional<Municipality> calendar = Optional.of(new Municipality());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
