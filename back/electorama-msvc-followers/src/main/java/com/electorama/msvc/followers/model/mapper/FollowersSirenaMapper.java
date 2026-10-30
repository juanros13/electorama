package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.FollowersSirenaDto;
import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

public class FollowersSirenaMapper extends BaseMapper<FollowersSirenaEntity, FollowersSirenaDto> {

  @Override
  public FollowersSirenaEntity convertToEntity(FollowersSirenaDto dto, Object... args) {
    FollowersSirenaEntity calendarEntity = new FollowersSirenaEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
      // calendarEntity.setCatCommittee(new
      // CatCommitteeMapper().convertToEntity(dto.getCatCommittee(), args));
    }
    return calendarEntity;
  }

  @Override
  public FollowersSirenaDto convertToDto(FollowersSirenaEntity entity, Object... args) {
    FollowersSirenaDto calendar = new FollowersSirenaDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
      // calendar.setCatCommittee(new CatCommitteeMapper().convertToDto(entity.getCatCommittee(),
      // args));
    }
    return calendar;
  }

  public Optional<FollowersSirenaDto> convertToDtoOptional(
      FollowersSirenaEntity entity, Object... args) {
    Optional<FollowersSirenaDto> calendar = Optional.of(new FollowersSirenaDto());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
      // calendar.get().setCatCommittee(new
      // CatCommitteeMapper().convertToDto(entity.getCatCommittee(), args));
    }
    return calendar;
  }

  @Override
  public Page<FollowersSirenaDto> convertToDtoPage(
      Page<FollowersSirenaEntity> entity, Object... args) {

    return super.convertToDtoPage(entity, args);
  }
}
