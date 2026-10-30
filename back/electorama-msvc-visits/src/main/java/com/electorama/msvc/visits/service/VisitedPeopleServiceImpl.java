package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.model.entity.VisitedNumberEntity;
import com.electorama.msvc.visits.model.entity.VisitedPeopleEntity;
import com.electorama.msvc.visits.model.mapper.VisitedMapper;
import com.electorama.msvc.visits.model.mapper.VisitedPeopleMapper;
import com.electorama.msvc.visits.model.repository.VisitedNumberRepository;
import com.electorama.msvc.visits.model.repository.VisitedPeopleRepository;
import com.electorama.msvc.visits.model.repository.VisitedRepository;
import com.electorama.msvc.visits.model.rest.SeccionResponse;
import com.electorama.msvc.visits.service.rest.GeographyRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class VisitedPeopleServiceImpl implements VisitedPeopleService {

  @Autowired private VisitedPeopleRepository visitedPeopleRepository;

  @Autowired private VisitedRepository visitedRepository;

  @Autowired private GeographyRestClient geographyRestClient;

  @Autowired private VisitedNumberRepository visitedNumberRepository;

  private final VisitedPeopleMapper visitedPeopleMapper = new VisitedPeopleMapper();

  private final VisitedMapper visitedMapper = new VisitedMapper();

  @Override
  public void delete(Long id) {}

  @Override
  public Optional<VisitedPeopleDto> byId(Long id, String authId) {
    return Optional.of(
        visitedPeopleMapper.convertToDto(
            visitedPeopleRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public VisitedPeopleDto save(VisitedPeopleDto visitedPeopleDto, String authId, Long id) {
    VisitedPeopleEntity visitedEnt = visitedPeopleMapper.convertToEntity(visitedPeopleDto);

    VisitedPeopleEntity vicitedEndSave = visitedPeopleRepository.save(visitedEnt);

    return visitedPeopleMapper.convertToDto(vicitedEndSave);
  }

  @Override
  public VisitedPeopleDto addVisit(VisitedPeopleDto visitedPeopleDto, String authId, Long id) {
    VisitedPeopleEntity visitedEnt = visitedPeopleMapper.convertToEntity(visitedPeopleDto);

    VisitedPeopleEntity visitedEndSave = visitedPeopleRepository.save(visitedEnt);

    VisitedNumberEntity visitedNumber = new VisitedNumberEntity();
    Optional<VisitedNumberEntity> visitedNumberMax =
        visitedNumberRepository.findFirstByVisitedPeopleIdOrderByVisitedNumberDesc(
            visitedEndSave.getId());
    if (visitedNumberMax.isPresent()) {
      visitedNumber.setVisitedNumber(visitedNumberMax.get().getVisitedNumber() + 1);
      visitedNumber.setVisitedPeopleId(visitedEndSave.getId());
      visitedNumberRepository.save(visitedNumber);
    }

    return visitedPeopleMapper.convertToDto(visitedEndSave);
  }

  @Override
  public Page<VisitedPeopleDto> listPeople(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authIdHeader) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<VisitedPeopleDto> dtoPage = null;
    Page<VisitedPeopleEntity> entities = null;
    if (search.length() >= 1) {
      entities = visitedPeopleRepository.findByParametros(authIdHeader, search, search, paging);
    } else {
      entities =
          visitedPeopleRepository
              .findByVisitedEntity_CoordinateZoneIdOrVisitedEntity_ReferenceAuthId(
                  authIdHeader, authIdHeader, paging);
    }
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    dtoPage =
        entities.map(
            entity -> {
              VisitedPeopleDto dto = new VisitedPeopleDto();

              if (entity != null) {
                dto = visitedPeopleMapper.convertToDto(entity);
                dto.setVisitedEntity(visitedMapper.convertToDto(entity.getVisitedEntity()));
              }

              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              entity
                                  .getVisitedEntity()
                                  .getSeccionId()
                                  .toString()
                                  .equals(seccion.getId().toString()))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              dto.setSeccionNumber(seccionNumber);
              return dto;
            });

    return dtoPage;
  }

  @Override
  public Page<VisitedPeopleDto> listPeopleAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<VisitedPeopleDto> dtoPage = null;
    Page<VisitedPeopleEntity> entities = null;
    if (search.length() >= 1) {
      entities = visitedPeopleRepository.findAllByParametros(search, search, paging);
    } else {
      entities = visitedPeopleRepository.findAll(paging);
    }

    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    dtoPage =
        entities.map(
            entity -> {
              VisitedPeopleDto dto = new VisitedPeopleDto();

              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              entity
                                  .getVisitedEntity()
                                  .getSeccionId()
                                  .toString()
                                  .equals(seccion.getId().toString()))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              if (entity != null) {
                dto = visitedPeopleMapper.convertToDto(entity);
                dto.setVisitedEntity(visitedMapper.convertToDto(entity.getVisitedEntity()));
              }
              dto.setSeccionNumber(seccionNumber);

              return dto;
            });

    return dtoPage;
  }
}
