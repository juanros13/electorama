package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallReferrerDto;
import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallReferrerEntity;
import org.springframework.beans.BeanUtils;

public class ReportCanvasAndWallsReferrerMapper
    extends BaseMapper<ReportCanvasAndWallReferrerEntity, ReportCanvasAndWallReferrerDto> {
  @Override
  public ReportCanvasAndWallReferrerEntity convertToEntity(
      ReportCanvasAndWallReferrerDto dto, Object... args) {
    ReportCanvasAndWallReferrerEntity entity = new ReportCanvasAndWallReferrerEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public ReportCanvasAndWallReferrerDto convertToDto(
      ReportCanvasAndWallReferrerEntity entity, Object... args) {
    ReportCanvasAndWallReferrerDto dto = new ReportCanvasAndWallReferrerDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
