package com.electorama.msvc.users.model.mapper;

import com.electorama.msvc.users.model.dto.UserSeccionDto;
import com.electorama.msvc.users.model.entity.UserSeccionEntity;
import org.springframework.beans.BeanUtils;

public class UserSeccionMapper extends BaseMapper<UserSeccionEntity, UserSeccionDto> {

  @Override
  public UserSeccionEntity convertToEntity(UserSeccionDto dto, Object... args) {
    UserSeccionEntity userEntity = new UserSeccionEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, userEntity);
    }
    return userEntity;
  }

  @Override
  public UserSeccionDto convertToDto(UserSeccionEntity entity, Object... args) {
    UserSeccionDto user = new UserSeccionDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, user);
    }
    return user;
  }
}
