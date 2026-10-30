package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface VisitedPeopleService {

  void delete(Long id);

  Optional<VisitedPeopleDto> byId(Long id, String authId);

  VisitedPeopleDto save(VisitedPeopleDto visitedPeopleDto, String authId, Long id);

  VisitedPeopleDto addVisit(VisitedPeopleDto visitedPeopleDto, String authId, Long id);

  Page<VisitedPeopleDto> listPeople(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authIdHeader);

  Page<VisitedPeopleDto> listPeopleAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);
}
