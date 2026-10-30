package com.electorama.msvc.elections.structure.model.dto;

import jakarta.persistence.*;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RouteAssignmentDto {

  private Long id;

  private RouteDto route;

  private Long pollingBoothId;

  private List<PollingRepresentativeDto> pollingRepresentatives;
}
