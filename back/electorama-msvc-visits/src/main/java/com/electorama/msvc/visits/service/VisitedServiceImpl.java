package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.VisitedDto;
import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.model.entity.VisitedEntity;
import com.electorama.msvc.visits.model.entity.VisitedNumberEntity;
import com.electorama.msvc.visits.model.entity.VisitedPeopleEntity;
import com.electorama.msvc.visits.model.mapper.VisitedMapper;
import com.electorama.msvc.visits.model.mapper.VisitedPeopleMapper;
import com.electorama.msvc.visits.model.repository.VisitedNumberRepository;
import com.electorama.msvc.visits.model.repository.VisitedPeopleRepository;
import com.electorama.msvc.visits.model.repository.VisitedRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class VisitedServiceImpl implements VisitedService {

  @Autowired private VisitedRepository visitedRepository;

  @Autowired private VisitedPeopleRepository visitedPeopleRepository;
  @Autowired private VisitedNumberRepository visitedNumberRepository;

  private final VisitedMapper visitedMapper = new VisitedMapper();

  private final VisitedPeopleMapper visitedPeopleMapper = new VisitedPeopleMapper();

  @Override
  public Page<VisitedDto> list(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<VisitedDto> dtoPage = null;
    //        if(search.length() >=1 ){
    //            entities = visitedRepository.findBySeccionId(search,
    //                    search,
    //                    search,
    //                    search,
    //                    search,
    //                    paging);
    //        }else{
    //            entities = visitedRepository.findByTramoId(paging);
    //        }
    Page<VisitedEntity> entities = visitedRepository.findAll(paging);
    dtoPage =
        entities.map(
            entity -> {
              VisitedDto dto = new VisitedDto();

              if (entity != null) {
                dto = visitedMapper.convertToDto(entity);
              }
              return dto;
            });

    return dtoPage;
  }

  @Override
  public VisitedDto save(VisitedDto visited, String authId) {
    VisitedEntity visitedEnt = visitedMapper.convertToEntity(visited, authId);
    visitedEnt.setUserCreatedId(authId);

    List<VisitedPeopleEntity> visitedPeopleEntities = visitedEnt.getVisitedPeople();
    visitedEnt.setVisitedPeople(new ArrayList<>());

    if (visitedPeopleEntities.size() > 0) {
      visitedEnt.setNumberOfVisited(visitedPeopleEntities.size());
    }
    VisitedDto visitedDto = visitedMapper.convertToDto(visitedRepository.save(visitedEnt));

    if (visitedPeopleEntities.size() > 0) {
      visitedPeopleEntities.stream()
          .map(
              people -> {
                people.setVisitedEntity(visitedMapper.convertToEntity(visitedDto));
                VisitedPeopleEntity visitedPeople = visitedPeopleRepository.save(people);
                VisitedNumberEntity visitedNumber = new VisitedNumberEntity();
                visitedNumber.setVisitedNumber(people.getVisitNumber());
                visitedNumber.setVisitedPeopleId(visitedPeople.getId());
                visitedNumberRepository.save(visitedNumber);
                return people;
              })
          .collect(Collectors.toList());
    }

    // visitedDto.setVisitedPeople(visitedPeopleMapper.convertToDtoList(visitedPeopleEntities));
    visitedDto.setVisitedPeople(visitedPeopleMapper.convertToDtoList(visitedPeopleEntities));
    return visitedDto;
  }

  @Override
  public VisitedDto update(VisitedDto visited, String authId) {
    VisitedEntity visitedEnt = visitedMapper.convertToEntity(visited, authId);
    visitedEnt.setUserCreatedId(authId);
    Integer visitadosExtra = visitedEnt.getNumberOfVisited();

    if (visited.getVisitedPeople().size() > 0) {
      for (VisitedPeopleDto visitedPeople : visited.getVisitedPeople()) {
        visitedPeople.setVisitedEntity(visited);
        if (visitedPeople.getId() == null) {
          visitadosExtra = visitadosExtra + 1;
        }
      }
    }
    visitedEnt.getVisitedPeople().stream()
        .map(
            people -> {
              people.setVisitedEntity(visitedEnt);
              // visitedPeopleRepository.save(people);
              return people;
            })
        .collect(Collectors.toList());

    visitedEnt.setNumberOfVisited(visitadosExtra);
    VisitedEntity visitEnt = visitedRepository.save(visitedEnt);
    VisitedDto visitedDto = visitedMapper.convertToDto(visitEnt);

    return visitedDto;
  }

  @Override
  public Optional<VisitedDto> byId(Long id, String authId) {
    VisitedEntity visitedEntity =
        visitedRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    return Optional.of(visitedMapper.convertToDto(visitedEntity));
  }

  @Override
  public void delete(Long id) {}

  @Override
  public Page<VisitedDto> xSectionList(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer idSection) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<VisitedDto> dtoPage = null;
    //        if(search.length() >=1 ){
    //            entities = visitedRepository.findBySeccionId(search,
    //                    search,
    //                    search,
    //                    search,
    //                    search,
    //                    paging);
    //        }else{
    //            entities = visitedRepository.findByTramoId(paging);
    //        }
    Page<VisitedEntity> entities = visitedRepository.findBySeccionId(idSection, paging);
    dtoPage =
        entities.map(
            entity -> {
              VisitedDto dto = new VisitedDto();

              if (entity != null) {
                dto = visitedMapper.convertToDto(entity);
              }
              return dto;
            });

    return dtoPage;
  }

  @Override
  public VisitedDto createVisited(VisitedDto visited, String authId) {
    VisitedEntity visitedEnt = visitedMapper.convertToEntity(visited, authId);
    visitedEnt.setUserCreatedId(authId);

    List<VisitedPeopleEntity> visitedPeopleEntities = visitedEnt.getVisitedPeople();
    visitedEnt.setVisitedPeople(new ArrayList<>());

    if (visitedPeopleEntities.size() > 0) {
      visitedEnt.setNumberOfVisited(visitedPeopleEntities.size());
    }
    VisitedDto visitedDto = visitedMapper.convertToDto(visitedRepository.save(visitedEnt));

    if (visitedPeopleEntities.size() > 0) {
      visitedPeopleEntities.stream()
          .map(
              people -> {
                people.setVisitedEntity(visitedMapper.convertToEntity(visitedDto));

                VisitedPeopleEntity peopleEntity =
                    visitedPeopleRepository.findByUuid(people.getUuid());

                if (peopleEntity != null) {
                  if (peopleEntity.getOpenTheDoor() == true) {
                    peopleEntity.setStreet(people.getStreet());
                    peopleEntity.setExternalNumber(people.getExternalNumber());
                    peopleEntity.setLongitude(people.getLongitude());
                    peopleEntity.setLatitude(people.getLatitude());
                    peopleEntity.setNames(people.getNames());
                    peopleEntity.setLastName(people.getLastName());
                    peopleEntity.setSurName(people.getSurName());
                    peopleEntity.setAdultsInHome(people.getAdultsInHome());
                    peopleEntity.setPhone(people.getPhone());
                    peopleEntity.setDeliveredGazette(people.getDeliveredGazette());
                    peopleEntity.setVisitNumber(people.getVisitNumber());
                    peopleEntity.setNotes(people.getNotes());
                    peopleEntity.setIsRelated(people.getIsRelated());
                    peopleEntity.setAgeOfThePersonWhoOpensTheDoor(
                        people.getAgeOfThePersonWhoOpensTheDoor());
                    peopleEntity.setSexOfTheOneWhoOpensTheDoor(
                        people.getSexOfTheOneWhoOpensTheDoor());

                  } else if (peopleEntity.getOpenTheDoor() == false) {
                    peopleEntity.setStreet(people.getStreet());
                    peopleEntity.setExternalNumber(people.getExternalNumber());
                    peopleEntity.setLongitude(people.getLongitude());
                    peopleEntity.setLatitude(people.getLatitude());
                    peopleEntity.setVisitNumber(people.getVisitNumber());
                  }
                }
                VisitedPeopleEntity visitedPeople = new VisitedPeopleEntity();

                if (peopleEntity != null)
                  visitedPeople = visitedPeopleRepository.save(peopleEntity);
                else visitedPeople = visitedPeopleRepository.save(people);

                VisitedNumberEntity visitedNumber = new VisitedNumberEntity();
                visitedNumber.setVisitedNumber(people.getVisitNumber());
                visitedNumber.setVisitedPeopleId(visitedPeople.getId());
                visitedNumberRepository.save(visitedNumber);
                return people;
              })
          .collect(Collectors.toList());
    }

    // visitedDto.setVisitedPeople(visitedPeopleMapper.convertToDtoList(visitedPeopleEntities));
    visitedDto.setVisitedPeople(visitedPeopleMapper.convertToDtoList(visitedPeopleEntities));
    return visitedDto;
  }
}
