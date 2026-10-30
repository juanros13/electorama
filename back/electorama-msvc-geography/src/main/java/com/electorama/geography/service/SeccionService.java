package com.electorama.geography.service;

import com.electorama.geography.model.dto.Seccion;
import java.util.List;
import java.util.Optional;

public interface SeccionService {

  Optional<Seccion> byId(Long id);

  List<Seccion> findByEntityId(Integer id);

  List<Seccion> findByFederalDistrictId(Integer id);

  List<Seccion> findBySectionsByFederalDistrict(Integer federalDistrictId);

  List<Seccion> findByLocalDistrictId(Integer LocalDistrictId);

  List<Seccion> findSectionByLocalDistrictId(Integer LocalDistrictId);

  List<Seccion> findByMunicipality(Integer id);

  List<Seccion> findByMunicipalityReference();

  List<Seccion> findBySections();

  Seccion save(Seccion seccion);
}
