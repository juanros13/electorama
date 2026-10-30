package com.electorama.geography.service;

import com.electorama.geography.model.dto.SafeHouse;
import java.util.List;
import java.util.Optional;

public interface SafeHouseService {
  Optional<SafeHouse> byId(Long id);

  List<SafeHouse> findByMunicipalities();

  SafeHouse save(SafeHouse safeHouse);

  SafeHouse delete(Long idSafeHouse);
}
