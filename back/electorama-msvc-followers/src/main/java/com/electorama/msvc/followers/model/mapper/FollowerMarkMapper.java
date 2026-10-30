package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.FollowerMarkDto;
import com.electorama.msvc.followers.model.entity.FollowerMarkEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class FollowerMarkMapper extends BaseMapper<FollowerMarkEntity, FollowerMarkDto> {

  @Override
  public FollowerMarkEntity convertToEntity(FollowerMarkDto dto, Object... args) {
    FollowerMarkEntity calendarEntity = new FollowerMarkEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public FollowerMarkDto convertToDto(FollowerMarkEntity entity, Object... args) {
    FollowerMarkDto calendar = new FollowerMarkDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<FollowerMarkDto> convertToDtoOptional(FollowerMarkEntity entity, Object... args) {
    Optional<FollowerMarkDto> calendar = Optional.of(new FollowerMarkDto());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
