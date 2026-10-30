package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallsGoalDto;
import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsGoalEntity;
import org.springframework.beans.BeanUtils;

public class CanvasAndWallsGoalMapper
    extends BaseMapper<CanvasAndWallsGoalEntity, CanvasAndWallsGoalDto> {
  private final CanvasAndWallsCategoryMapper canvasAndWallsCategoryMapper =
      new CanvasAndWallsCategoryMapper();

  private final CanvasAndWallsProyectMapper canvasAndWallsProyectMapper =
      new CanvasAndWallsProyectMapper();

  @Override
  public CanvasAndWallsGoalEntity convertToEntity(CanvasAndWallsGoalDto dto, Object... args) {
    CanvasAndWallsGoalEntity entity = new CanvasAndWallsGoalEntity();
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
  public CanvasAndWallsGoalDto convertToDto(CanvasAndWallsGoalEntity entity, Object... args) {
    CanvasAndWallsGoalDto dto = new CanvasAndWallsGoalDto();
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
