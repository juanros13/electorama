package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.VisitedNumberDto;
import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import java.util.List;
import java.util.Optional;

public interface VisitedNumberService {

  List<VisitedNumberDto> byVisitedPeopleId(Long id);

  boolean delete(Long id, VisitedPeopleDto visitedPeople);

  Optional<VisitedNumberDto> byId(Long id);
}
