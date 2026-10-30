package com.electorama.msvc.documentmanagement.models.mapper;

import com.electorama.msvc.documentmanagement.models.dto.PersonDto;
import com.electorama.msvc.documentmanagement.models.entity.PersonEntity;
import org.springframework.beans.BeanUtils;

public class PersonMapper extends BaseMapper<PersonEntity, PersonDto> {

  @Override
  public PersonEntity convertToEntity(PersonDto dto, Object... args) {
    PersonEntity entity = new PersonEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public PersonDto convertToDto(PersonEntity entity, Object... args) {
    PersonDto dto = new PersonDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
