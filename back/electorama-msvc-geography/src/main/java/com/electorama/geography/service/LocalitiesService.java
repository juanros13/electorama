package com.electorama.geography.service;

import com.electorama.geography.model.dto.LocalitiesSection;
import com.electorama.geography.model.entity.LocalitiesSectionEntity;
import java.util.List;
import org.springframework.data.domain.Page;

public interface LocalitiesService {

  Page<LocalitiesSection> findByLocalities(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search);

  List<LocalitiesSectionEntity> findByLocalitiesByMunicipality(String id);
}
