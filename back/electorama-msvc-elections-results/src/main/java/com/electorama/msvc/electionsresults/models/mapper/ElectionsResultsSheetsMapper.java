package com.electorama.msvc.electionsresults.models.mapper;

import com.electorama.msvc.electionsresults.models.dto.ElectionsResultsSheetsDto;
import com.electorama.msvc.electionsresults.models.entity.ElectionResultsSheetsEntity;
import org.springframework.beans.BeanUtils;

public class ElectionsResultsSheetsMapper
    extends BaseMapper<ElectionResultsSheetsEntity, ElectionsResultsSheetsDto> {

  @Override
  public ElectionResultsSheetsEntity convertToEntity(
      ElectionsResultsSheetsDto dto, Object... args) {
    ElectionResultsSheetsEntity electionsEntity = new ElectionResultsSheetsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, electionsEntity);
    }
    return electionsEntity;
  }

  @Override
  public ElectionsResultsSheetsDto convertToDto(
      ElectionResultsSheetsEntity entity, Object... args) {
    ElectionsResultsSheetsDto ElectionsResultsSheetsDto = new ElectionsResultsSheetsDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, ElectionsResultsSheetsDto);
    }
    return ElectionsResultsSheetsDto;
  }
}
