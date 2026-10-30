package com.electorama.msvc.elections.structure.service;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeCoordinatorDto;
import java.util.List;
import java.util.Optional;

public interface PollingRepresentativeCoordinatorService {
  List<PollingRepresentativeCoordinatorDto> getAllCoordinators();

  Optional<PollingRepresentativeCoordinatorDto> getByAuthId(String authIdHeader);
}
