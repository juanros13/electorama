package com.electorama.msvc.elections.structure.service;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeAssociationDto;
import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import com.electorama.msvc.elections.structure.model.entity.RouteAssignmentEntity;
import com.electorama.msvc.elections.structure.model.entity.RouteEntity;
import com.electorama.msvc.elections.structure.model.mapper.PollingRepresentativeAssociationMapper;
import com.electorama.msvc.elections.structure.model.mapper.PollingRepresentativeMapper;
import com.electorama.msvc.elections.structure.model.repository.PollingRepresentativeRepository;
import com.electorama.msvc.elections.structure.model.repository.RouteAssignmentRepository;
import com.electorama.msvc.elections.structure.model.repository.RouteRepository;
import com.electorama.msvc.elections.structure.model.rest.elections.PollingBoothResponse;
import com.electorama.msvc.elections.structure.service.rest.ElectionsRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PollingRepresentativeServiceImpl implements PollingRepresentativeService {

  @Autowired PollingRepresentativeRepository pollingRepresentativeRepository;

  @Autowired ElectionsRestClient electionsRestClient;

  @Autowired RouteAssignmentRepository routeAssignmentRepository;

  @Autowired RouteRepository routeRepository;

  private final PollingRepresentativeMapper pollingRepresentativeMapper =
      new PollingRepresentativeMapper();

  private final PollingRepresentativeAssociationMapper pollingRepresentativeAssociationMapper =
      new PollingRepresentativeAssociationMapper();

  @Override
  public Optional<PollingRepresentativeDto> get(Long id) {
    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeRepository.findById(id).map(pollingRepresentativeMapper::convertToDto);

    return pollingRepresentativeDto;
  }

  @Override
  public Optional<PollingRepresentativeDto> getByAuthId(String authId) {
    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeRepository
            .findByAuthId(authId)
            .map(pollingRepresentativeMapper::convertToDto);

    return pollingRepresentativeDto;
  }

  @Override
  public List<PollingRepresentativeDto> getByPollingBooth(Long id) {
    List<PollingRepresentativeDto> pollingRepresentativeDtoList =
        pollingRepresentativeMapper.convertToDtoList(
            pollingRepresentativeRepository.findByPollingBoothId(id));
    return pollingRepresentativeDtoList;
  }

  @Override
  public PollingRepresentativeAssociationDto getPollingRepresentativeAssociation(String authId) {
    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeRepository
            .findByAuthId(authId)
            .map(pollingRepresentativeMapper::convertToDto);

    if (pollingRepresentativeDto.isPresent()) {
      PollingRepresentativeAssociationDto pollingRepresentativeAssociationDto =
          pollingRepresentativeAssociationMapper.convertToPollingRepresentativeAssociation(
              pollingRepresentativeDto.get());
      List<PollingBoothResponse> pollingBoothResponsesList = new ArrayList<>();
      PollingBoothResponse pollingBoothResponses =
          electionsRestClient.getPollingBoothById(
              pollingRepresentativeAssociationDto.getPollingBoothId());
      pollingBoothResponsesList.add(pollingBoothResponses);
      pollingRepresentativeAssociationDto.setPollingBoothList(pollingBoothResponsesList);
      return pollingRepresentativeAssociationDto;
    }
    return null;
  }

  @Override
  public PollingRepresentativeAssociationDto getPollingRepresentativeAssociationByRg(
      String authId) {
    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeRepository
            .findByAuthId(authId)
            .map(pollingRepresentativeMapper::convertToDto);
    if (pollingRepresentativeDto.isPresent()) {
      PollingRepresentativeAssociationDto pollingRepresentativeAssociationDto =
          pollingRepresentativeAssociationMapper.convertToPollingRepresentativeAssociation(
              pollingRepresentativeDto.get());
      List<PollingBoothResponse> pollingBoothResponsesList = new ArrayList<>();
      Optional<RouteEntity> routeEntityOptional =
          routeRepository.findByPollingRepresentativeGeneral_AuthId(authId);
      if (routeEntityOptional.isPresent()) {
        RouteEntity route = routeEntityOptional.get();

        List<RouteAssignmentEntity> routeAssignmentEntities =
            routeAssignmentRepository.findByRoute_Id(route.getId());
        if (!routeAssignmentEntities.isEmpty()) {
          routeAssignmentEntities.forEach(
              routeAssignmentEntity -> {
                PollingBoothResponse pollingBoothResponses =
                    electionsRestClient.getPollingBoothById(
                        routeAssignmentEntity.getPollingBoothId());
                pollingBoothResponsesList.add(pollingBoothResponses);
              });
          pollingRepresentativeAssociationDto.setPollingBoothList(pollingBoothResponsesList);
          return pollingRepresentativeAssociationDto;
        }
      }
    }
    return null;
  }
}
