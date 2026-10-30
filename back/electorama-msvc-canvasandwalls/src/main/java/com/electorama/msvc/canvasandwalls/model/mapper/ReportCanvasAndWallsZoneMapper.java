package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallZoneDto;
import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallZoneEntity;
import org.springframework.beans.BeanUtils;

public class ReportCanvasAndWallsZoneMapper
    extends BaseMapper<ReportCanvasAndWallZoneEntity, ReportCanvasAndWallZoneDto> {
  @Override
  public ReportCanvasAndWallZoneEntity convertToEntity(
      ReportCanvasAndWallZoneDto dto, Object... args) {
    ReportCanvasAndWallZoneEntity entity = new ReportCanvasAndWallZoneEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public ReportCanvasAndWallZoneDto convertToDto(
      ReportCanvasAndWallZoneEntity entity, Object... args) {
    ReportCanvasAndWallZoneDto dto = new ReportCanvasAndWallZoneDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
