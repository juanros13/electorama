package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.VisitedNumberDto;
import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.model.entity.VisitedNumberEntity;
import com.electorama.msvc.visits.model.entity.VisitedPeopleEntity;
import com.electorama.msvc.visits.model.mapper.VisitedNumberMapper;
import com.electorama.msvc.visits.model.mapper.VisitedPeopleMapper;
import com.electorama.msvc.visits.model.repository.VisitedNumberRepository;
import com.electorama.msvc.visits.model.repository.VisitedPeopleRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class VisitedNumberServiceImpl implements VisitedNumberService {

  @Autowired private VisitedNumberRepository visitedNumberRepository;

  @Autowired private VisitedPeopleRepository visitedPeopleRepository;

  private final VisitedPeopleMapper visitedPeopleMapper = new VisitedPeopleMapper();

  private final VisitedNumberMapper visitedNumberMapper = new VisitedNumberMapper();

  @Override
  public List<VisitedNumberDto> byVisitedPeopleId(Long id) {
    return visitedNumberMapper.convertToDtoList(visitedNumberRepository.findByVisitedPeopleId(id));
  }

  @Override
  public boolean delete(Long id, VisitedPeopleDto visitedPeople) {
    try {
      VisitedPeopleEntity visitedEnt = visitedPeopleMapper.convertToEntity(visitedPeople);
      VisitedNumberEntity visitedNumber = new VisitedNumberEntity();
      visitedNumberRepository.deleteById(id);
      VisitedNumberEntity visitedNumberMax =
          visitedNumberRepository
              .findFirstByVisitedPeopleIdOrderByVisitedNumberDesc(visitedPeople.getId())
              .orElseThrow(EntityNotFoundException::new);
      visitedEnt.setVisitNumber(visitedNumberMax.getVisitedNumber());
      visitedPeopleRepository.save(visitedEnt);
      return true;
    } catch (DataAccessException ex) {
      return false;
    }
  }

  @Override
  public Optional<VisitedNumberDto> byId(Long id) {
    VisitedNumberEntity visitedNumberEntity =
        visitedNumberRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    return Optional.of(visitedNumberMapper.convertToDto(visitedNumberEntity));
  }
}
