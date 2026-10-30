package com.electorama.geography.service;

import com.electorama.geography.model.dto.FederalDistrict;
import java.util.List;
import java.util.Optional;

public interface FederalDistrictService {

  Optional<FederalDistrict> byId(Long id);

  List<FederalDistrict> findByEntityId(Integer id);

  List<FederalDistrict> findByFederalDistricts();
}
