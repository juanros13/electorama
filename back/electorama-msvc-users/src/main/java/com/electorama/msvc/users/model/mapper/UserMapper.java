package com.electorama.msvc.users.model.mapper;

import com.electorama.msvc.users.model.dto.User;
import com.electorama.msvc.users.model.entity.UserEntity;
import org.springframework.beans.BeanUtils;

public class UserMapper extends BaseMapper<UserEntity, User> {

  @Override
  public UserEntity convertToEntity(User dto, Object... args) {
    UserEntity entity = new UserEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public User convertToDto(UserEntity entity, Object... args) {
    User user = new User();
    if (entity != null) {
      BeanUtils.copyProperties(entity, user);
    }
    return user;
  }
}
