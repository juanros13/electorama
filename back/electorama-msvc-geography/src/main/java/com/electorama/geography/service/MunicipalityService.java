package com.electorama.geography.service;

import com.electorama.geography.model.dto.Municipality;
import java.util.List;
import java.util.Optional;

public interface MunicipalityService {

  Optional<Municipality> byId(Long id);

  List<Municipality> findByEntityId(Integer id);

  List<Municipality> findByMunicipalities();
}
