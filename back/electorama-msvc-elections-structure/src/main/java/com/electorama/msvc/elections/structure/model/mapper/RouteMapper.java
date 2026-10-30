package com.electorama.msvc.elections.structure.model.mapper;

import com.electorama.msvc.elections.structure.model.dto.RouteDto;
import com.electorama.msvc.elections.structure.model.entity.RouteEntity;
import org.springframework.beans.BeanUtils;

public class RouteMapper extends BaseMapper<RouteEntity, RouteDto> {

  private final PollingRepresentativeMapper pollingRepresentativeMapper =
      new PollingRepresentativeMapper();

  private final RouteAssignmentMapper routeAssignmentMapper = new RouteAssignmentMapper();

  @Override
  public RouteEntity convertToEntity(RouteDto dto, Object... args) {
    RouteEntity entity = new RouteEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, entity);
      if (dto.getPollingRepresentativeGeneral() != null) {
        entity.setPollingRepresentativeGeneral(
            pollingRepresentativeMapper.convertToEntity(dto.getPollingRepresentativeGeneral()));
      }
      if (dto.getRouteAssignment() != null) {
        entity.setRouteAssignment(
            routeAssignmentMapper.convertToEntityList(dto.getRouteAssignment()));
      }
    }
    return entity;
  }

  @Override
  public RouteDto convertToDto(RouteEntity entity, Object... args) {
    RouteDto dto = new RouteDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, dto);
      if (entity.getPollingRepresentativeGeneral() != null) {
        dto.setPollingRepresentativeGeneral(
            pollingRepresentativeMapper.convertToDto(entity.getPollingRepresentativeGeneral()));
      }
      if (entity.getRouteAssignment() != null) {
        dto.setRouteAssignment(
            new RouteAssignmentMapper().convertToDtoList(entity.getRouteAssignment()));
      }
    }
    return dto;
  }
}
