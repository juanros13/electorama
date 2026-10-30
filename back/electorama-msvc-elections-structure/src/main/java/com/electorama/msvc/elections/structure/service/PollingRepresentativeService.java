package com.electorama.msvc.elections.structure.service;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeAssociationDto;
import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import java.util.List;
import java.util.Optional;

public interface PollingRepresentativeService {

  Optional<PollingRepresentativeDto> get(Long id);

  Optional<PollingRepresentativeDto> getByAuthId(String id);

  List<PollingRepresentativeDto> getByPollingBooth(Long id);

  PollingRepresentativeAssociationDto getPollingRepresentativeAssociation(String authId);

  PollingRepresentativeAssociationDto getPollingRepresentativeAssociationByRg(String authId);
}
