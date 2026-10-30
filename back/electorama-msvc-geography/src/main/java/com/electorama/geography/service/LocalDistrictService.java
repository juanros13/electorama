package com.electorama.geography.service;

import com.electorama.geography.model.dto.LocalDistrict;
import java.util.List;
import java.util.Optional;

public interface LocalDistrictService {

  Optional<LocalDistrict> byId(Long id);

  List<LocalDistrict> findByEntityId(Integer id);

  List<LocalDistrict> findByLocalDistricts();
}
