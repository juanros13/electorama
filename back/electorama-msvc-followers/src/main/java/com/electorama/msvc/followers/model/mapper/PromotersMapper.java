package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.PromotersDto;
import com.electorama.msvc.followers.model.entity.PromotersEntity;
import java.util.Optional;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;

public class PromotersMapper extends BaseMapper<PromotersEntity, PromotersDto> {

  @Override
  public PromotersEntity convertToEntity(PromotersDto dto, Object... args) {
    PromotersEntity calendarEntity = new PromotersEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
      //      calendarEntity.setCatCommittee(
      //          new CatCommitteeMapper().convertToEntity(dto.getCatCommittee(), args));
    }
    return calendarEntity;
  }

  @Override
  public PromotersDto convertToDto(PromotersEntity entity, Object... args) {
    PromotersDto calendar = new PromotersDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
      //      calendar.setCatCommittee(
      //          new CatCommitteeMapper().convertToDto(entity.getCatCommittee(), args));
    }
    return calendar;
  }

  public Optional<PromotersDto> convertToDtoOptional(PromotersEntity entity, Object... args) {
    Optional<PromotersDto> calendar = Optional.of(new PromotersDto());
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
      //      calendar
      //          .get()
      //          .setCatCommittee(new CatCommitteeMapper().convertToDto(entity.getCatCommittee(),
      // args));
    }
    return calendar;
  }

  @Override
  public Page<PromotersDto> convertToDtoPage(Page<PromotersEntity> entity, Object... args) {

    return super.convertToDtoPage(entity, args);
  }
}
