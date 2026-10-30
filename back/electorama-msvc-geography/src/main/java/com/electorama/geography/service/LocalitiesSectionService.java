package com.electorama.geography.service;

import com.electorama.geography.model.dto.LocalitiesSection;
import com.electorama.geography.model.dto.SectionLocalities;
import java.util.List;
import java.util.Optional;

public interface LocalitiesSectionService {

  Optional<LocalitiesSection> byId(Long id);

  List<LocalitiesSection> getAll();

  List<LocalitiesSection> findBySectionId(Long id);

  LocalitiesSection createLocation(LocalitiesSection localitiesSection);

  LocalitiesSection save(LocalitiesSection localitiesSection);

  void delete(Long id);

  List<SectionLocalities> findBySection(Long id);
}
