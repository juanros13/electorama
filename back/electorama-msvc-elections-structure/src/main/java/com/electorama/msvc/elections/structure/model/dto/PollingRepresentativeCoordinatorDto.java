package com.electorama.msvc.elections.structure.model.dto;

import com.electorama.msvc.elections.structure.model.entity.PollingRepresentativeEntity;
import lombok.Data;

@Data
public class PollingRepresentativeCoordinatorDto {

  private Long id;

  private PollingRepresentativeEntity pollingRepresentativeGeneral;

  private PollingRepresentativeEntity pollingRepresentativeCoordinator;
}
