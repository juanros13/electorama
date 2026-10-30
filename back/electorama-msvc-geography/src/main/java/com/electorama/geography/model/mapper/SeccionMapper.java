package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.Seccion;
import com.electorama.geography.model.entity.SeccionEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class SeccionMapper extends BaseMapper<SeccionEntity, Seccion> {

  @Override
  public SeccionEntity convertToEntity(Seccion dto, Object... args) {
    SeccionEntity calendarEntity = new SeccionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public Seccion convertToDto(SeccionEntity entity, Object... args) {
    Seccion calendar = new Seccion();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<Seccion> convertToDtoOptional(SeccionEntity entity, Object... args) {
    Optional<Seccion> calendar = Optional.of(new Seccion());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
