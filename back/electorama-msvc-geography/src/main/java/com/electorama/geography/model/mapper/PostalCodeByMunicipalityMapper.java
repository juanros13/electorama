package com.electorama.geography.model.mapper;

import com.electorama.geography.model.dto.PostalCodeByMunicipality;
import com.electorama.geography.model.entity.PostalCodeByMunicipalityEntity;
import org.springframework.beans.BeanUtils;

public class PostalCodeByMunicipalityMapper
    extends BaseMapper<PostalCodeByMunicipalityEntity, PostalCodeByMunicipality> {

  @Override
  public PostalCodeByMunicipalityEntity convertToEntity(
      PostalCodeByMunicipality dto, Object... args) {
    PostalCodeByMunicipalityEntity calendarEntity = new PostalCodeByMunicipalityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
    }
    return calendarEntity;
  }

  @Override
  public PostalCodeByMunicipality convertToDto(
      PostalCodeByMunicipalityEntity entity, Object... args) {
    PostalCodeByMunicipality calendar = new PostalCodeByMunicipality();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
    }
    return calendar;
  }
}
