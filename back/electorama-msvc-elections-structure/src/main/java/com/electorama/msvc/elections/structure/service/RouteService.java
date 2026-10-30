package com.electorama.msvc.elections.structure.service;

import com.electorama.msvc.elections.structure.model.dto.RouteDto;
import java.util.Optional;

public interface RouteService {

  Optional<RouteDto> getById(Long idRoute);

  Optional<RouteDto> getByPollingRepresentativeGeneralId(Long idRepresentativeGeneral);

  Optional<RouteDto> getByPollingRepresentativeId(Long idRepresentative);
}
