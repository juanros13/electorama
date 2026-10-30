package com.electorama.msvc.elections.structure.service;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeCoordinatorDto;
import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import com.electorama.msvc.elections.structure.model.mapper.PollingRepresentativeCoordinatorMapper;
import com.electorama.msvc.elections.structure.model.mapper.PollingRepresentativeMapper;
import com.electorama.msvc.elections.structure.model.repository.PollingRepresentativeCoordinatorRepository;
import com.electorama.msvc.elections.structure.model.repository.PollingRepresentativeRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PollingRepresentativeCoordinatorServiceImpl
    implements PollingRepresentativeCoordinatorService {

  @Autowired
  private PollingRepresentativeCoordinatorRepository pollingRepresentativeCoordinatorRepository;

  @Autowired private PollingRepresentativeRepository pollingRepresentativeRepository;

  private final PollingRepresentativeMapper pollingRepresentativeMapper =
      new PollingRepresentativeMapper();
  private final PollingRepresentativeCoordinatorMapper pollingRepresentativeCoordinatorMapper =
      new PollingRepresentativeCoordinatorMapper();

  @Override
  public List<PollingRepresentativeCoordinatorDto> getAllCoordinators() {
    return pollingRepresentativeCoordinatorMapper.convertToDtoList(
        pollingRepresentativeCoordinatorRepository.findAll());
  }

  @Override
  public Optional<PollingRepresentativeCoordinatorDto> getByAuthId(String authIdHeader) {
    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeRepository
            .findByAuthId(authIdHeader)
            .map(pollingRepresentativeMapper::convertToDto);
    if (pollingRepresentativeDto.isPresent()) {
      Optional<PollingRepresentativeCoordinatorDto> pollingRepresentativeCoordinatorDto =
          pollingRepresentativeCoordinatorRepository
              .findByPollingRepresentativeCoordinator_Id(pollingRepresentativeDto.get().getId())
              .map(pollingRepresentativeCoordinatorMapper::convertToDto);
      return pollingRepresentativeCoordinatorDto;
    }

    return Optional.empty();
  }
}
