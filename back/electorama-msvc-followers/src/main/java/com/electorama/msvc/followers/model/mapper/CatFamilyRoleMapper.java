package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.CatFamilyRoleDto;
import com.electorama.msvc.followers.model.entity.CatFamilyRoleEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;

public class CatFamilyRoleMapper extends BaseMapper<CatFamilyRoleEntity, CatFamilyRoleDto> {

  @Override
  public CatFamilyRoleEntity convertToEntity(CatFamilyRoleDto dto, Object... args) {
    CatFamilyRoleEntity calendarEntity = new CatFamilyRoleEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public CatFamilyRoleDto convertToDto(CatFamilyRoleEntity entity, Object... args) {
    CatFamilyRoleDto calendar = new CatFamilyRoleDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }

  public Optional<CatFamilyRoleDto> convertToDtoOptional(
      CatFamilyRoleEntity entity, Object... args) {
    Optional<CatFamilyRoleDto> calendar = Optional.of(new CatFamilyRoleDto());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
