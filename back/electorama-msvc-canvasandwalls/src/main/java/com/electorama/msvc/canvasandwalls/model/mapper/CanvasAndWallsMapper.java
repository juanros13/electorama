package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallsDto;
import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsEntity;
import org.springframework.beans.BeanUtils;

public class CanvasAndWallsMapper extends BaseMapper<CanvasAndWallsEntity, CanvasAndWallsDto> {

  private final CanvasAndWallsCategoryMapper canvasAndWallsCategoryMapper =
      new CanvasAndWallsCategoryMapper();

  private final CanvasAndWallsProyectMapper canvasAndWallsProyectMapper =
      new CanvasAndWallsProyectMapper();

  @Override
  public CanvasAndWallsEntity convertToEntity(CanvasAndWallsDto dto, Object... args) {
    CanvasAndWallsEntity entity = new CanvasAndWallsEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      entity.setCanvasAndWallsCategory(
          canvasAndWallsCategoryMapper.convertToEntity(dto.getCanvasAndWallsCategory()));
      entity.setCanvasAndWallsProyect(
          canvasAndWallsProyectMapper.convertToEntity(dto.getCanvasAndWallsProyect()));
    }
    return entity;
  }

  @Override
  public CanvasAndWallsDto convertToDto(CanvasAndWallsEntity entity, Object... args) {
    CanvasAndWallsDto dto = new CanvasAndWallsDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      dto.setCanvasAndWallsCategory(
          canvasAndWallsCategoryMapper.convertToDto(entity.getCanvasAndWallsCategory()));
      dto.setCanvasAndWallsProyect(
          canvasAndWallsProyectMapper.convertToDto(entity.getCanvasAndWallsProyect()));
    }
    return dto;
  }
}
