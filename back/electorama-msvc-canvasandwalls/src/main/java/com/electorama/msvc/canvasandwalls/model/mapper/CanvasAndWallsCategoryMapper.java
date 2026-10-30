package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallCategoryDto;
import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsCategoryEntity;
import org.springframework.beans.BeanUtils;

public class CanvasAndWallsCategoryMapper
    extends BaseMapper<CanvasAndWallsCategoryEntity, CanvasAndWallCategoryDto> {
  @Override
  public CanvasAndWallsCategoryEntity convertToEntity(
      CanvasAndWallCategoryDto dto, Object... args) {
    CanvasAndWallsCategoryEntity entity = new CanvasAndWallsCategoryEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public CanvasAndWallCategoryDto convertToDto(
      CanvasAndWallsCategoryEntity entity, Object... args) {
    CanvasAndWallCategoryDto dto = new CanvasAndWallCategoryDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
