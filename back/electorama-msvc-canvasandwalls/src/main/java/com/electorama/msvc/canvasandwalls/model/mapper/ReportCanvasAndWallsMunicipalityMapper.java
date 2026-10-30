package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallMunicipalityDto;
import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallMunicipalityEntity;
import org.springframework.beans.BeanUtils;

public class ReportCanvasAndWallsMunicipalityMapper
    extends BaseMapper<ReportCanvasAndWallMunicipalityEntity, ReportCanvasAndWallMunicipalityDto> {
  @Override
  public ReportCanvasAndWallMunicipalityEntity convertToEntity(
      ReportCanvasAndWallMunicipalityDto dto, Object... args) {
    ReportCanvasAndWallMunicipalityEntity entity = new ReportCanvasAndWallMunicipalityEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public ReportCanvasAndWallMunicipalityDto convertToDto(
      ReportCanvasAndWallMunicipalityEntity entity, Object... args) {
    ReportCanvasAndWallMunicipalityDto dto = new ReportCanvasAndWallMunicipalityDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
