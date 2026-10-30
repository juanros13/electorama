package com.electorama.msvc.elections.structure.service;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import com.electorama.msvc.elections.structure.model.dto.RouteDto;
import com.electorama.msvc.elections.structure.model.mapper.PollingRepresentativeMapper;
import com.electorama.msvc.elections.structure.model.mapper.RouteMapper;
import com.electorama.msvc.elections.structure.model.repository.PollingRepresentativeRepository;
import com.electorama.msvc.elections.structure.model.repository.RouteRepository;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RouteServiceImpl implements RouteService {

  @Autowired RouteRepository routeRepository;

  @Autowired PollingRepresentativeRepository pollingRepresentativeRepository;

  private final RouteMapper routeMapper = new RouteMapper();

  private final PollingRepresentativeMapper pollingRepresentativeMapper =
      new PollingRepresentativeMapper();

  @Override
  public Optional<RouteDto> getById(Long idRoute) {
    Optional<RouteDto> routeDto = routeRepository.findById(idRoute).map(routeMapper::convertToDto);

    if (routeDto.isPresent()) {
      routeDto.get().getRouteAssignment().stream()
          .map(
              routeAssignmentDto -> {
                routeAssignmentDto.setPollingRepresentatives(
                    pollingRepresentativeMapper.convertToDtoList(
                        pollingRepresentativeRepository.findByPollingBoothId(
                            routeAssignmentDto.getPollingBoothId())));
                return routeAssignmentDto;
              })
          .collect(Collectors.toList());
    }
    return routeDto;
  }

  @Override
  public Optional<RouteDto> getByPollingRepresentativeGeneralId(Long idRoute) {
    Optional<RouteDto> routeDto =
        routeRepository
            .findByPollingRepresentativeGeneralId(idRoute)
            .map(routeMapper::convertToDto);

    if (routeDto.isPresent()) {
      routeDto.get().getRouteAssignment().stream()
          .map(
              routeAssignmentDto -> {
                routeAssignmentDto.setPollingRepresentatives(
                    pollingRepresentativeMapper.convertToDtoList(
                        pollingRepresentativeRepository.findByPollingBoothId(
                            routeAssignmentDto.getPollingBoothId())));
                return routeAssignmentDto;
              })
          .collect(Collectors.toList());
    }
    return routeDto;
  }

  @Override
  public Optional<RouteDto> getByPollingRepresentativeId(Long idRepresentative) {
    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeRepository
            .findById(idRepresentative)
            .map(pollingRepresentativeMapper::convertToDto);
    Optional<RouteDto> routeDto = Optional.empty();
    if (pollingRepresentativeDto.isPresent()
        && pollingRepresentativeDto.get().getPollingBoothId() != null) {
      routeDto =
          routeRepository
              .findByPollingBooth(pollingRepresentativeDto.get().getPollingBoothId())
              .map(routeMapper::convertToDto);

      if (routeDto.isPresent()) {
        routeDto.get().getRouteAssignment().stream()
            .map(
                routeAssignmentDto -> {
                  routeAssignmentDto.setPollingRepresentatives(
                      pollingRepresentativeMapper.convertToDtoList(
                          pollingRepresentativeRepository.findByPollingBoothId(
                              routeAssignmentDto.getPollingBoothId())));
                  return routeAssignmentDto;
                })
            .collect(Collectors.toList());
      }
    }

    return routeDto;
  }
}
