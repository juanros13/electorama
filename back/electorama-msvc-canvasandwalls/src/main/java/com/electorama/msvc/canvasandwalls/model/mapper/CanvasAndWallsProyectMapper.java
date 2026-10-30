package com.electorama.msvc.canvasandwalls.model.mapper;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallProyectDto;
import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsProyectEntity;
import org.springframework.beans.BeanUtils;

public class CanvasAndWallsProyectMapper
    extends BaseMapper<CanvasAndWallsProyectEntity, CanvasAndWallProyectDto> {
  @Override
  public CanvasAndWallsProyectEntity convertToEntity(CanvasAndWallProyectDto dto, Object... args) {
    CanvasAndWallsProyectEntity entity = new CanvasAndWallsProyectEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
    }
    return entity;
  }

  @Override
  public CanvasAndWallProyectDto convertToDto(CanvasAndWallsProyectEntity entity, Object... args) {
    CanvasAndWallProyectDto dto = new CanvasAndWallProyectDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
    }
    return dto;
  }
}
