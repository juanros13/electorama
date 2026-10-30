package com.electorama.msvc.elections.structure.model.dto;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteDto {

  private Long id;

  private String name;
  private Integer key;

  private List<RouteAssignmentDto> routeAssignment;

  private PollingRepresentativeDto pollingRepresentativeGeneral;
}
