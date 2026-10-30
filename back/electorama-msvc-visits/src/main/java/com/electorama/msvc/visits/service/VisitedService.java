package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.VisitedDto;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface VisitedService {

  Page<VisitedDto> list(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  VisitedDto save(VisitedDto followers, String authId);

  VisitedDto update(VisitedDto followers, String authId);

  Optional<VisitedDto> byId(Long id, String authId);

  void delete(Long id);

  Page<VisitedDto> xSectionList(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      Integer idSection);

  VisitedDto createVisited(VisitedDto followers, String authId);
}
