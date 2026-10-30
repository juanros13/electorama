package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.RouteAssignmentDto;
import com.electorama.msvc.elections.structure.model.entity.RouteAssignmentEntity;
import org.springframework.beans.BeanUtils;

public class RouteAssignmentMapper extends BaseMapper<RouteAssignmentEntity, RouteAssignmentDto> {

  @Override
  public RouteAssignmentEntity convertToEntity(RouteAssignmentDto dto, Object... args) {
    RouteAssignmentEntity entity = new RouteAssignmentEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getRoute() != null) {
        // entity.setRoute(
        //        routeMapper.convertToEntity(dto.getRoute()));
      }
    }

    return entity;
  }

  @Override
  public RouteAssignmentDto convertToDto(RouteAssignmentEntity entity, Object... args) {
    RouteAssignmentDto dto = new RouteAssignmentDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getRoute() != null) {
        // dto.setRoute(
        //        routeMapper.convertToDto(entity.getRoute()));
      }
    }
    return dto;
  }
}
