package com.electorama.geography.service;

import com.electorama.geography.exception.EntityNotFoundException;
import com.electorama.geography.model.dto.Municipality;
import com.electorama.geography.model.mapper.MunicipalityMapper;
import com.electorama.geography.model.repository.MunicipalityRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class MunicipalityServiceImpl implements MunicipalityService {

  @Autowired private MunicipalityRepository municipalityRepository;

  private final MunicipalityMapper municipalityMapper = new MunicipalityMapper();

  // Catálogo de municipios: casi inmutable -> cacheable para evitar el N+1 de los consumidores.
  @Override
  @Cacheable("municipalityById")
  public Optional<Municipality> byId(Long id) {
    return Optional.of(
        municipalityMapper.convertToDto(
            municipalityRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  @Cacheable("municipalitiesByEntity")
  public List<Municipality> findByEntityId(Integer id) {
    return new ArrayList<>(
        municipalityMapper.convertToDto(
            municipalityRepository.findByEntidadIdOrderByNomMunAsc(id)));
  }

  @Override
  @Cacheable("allMunicipalities")
  public List<Municipality> findByMunicipalities() {

    return municipalityMapper.convertToDtoList(municipalityRepository.findAll());
  }
}
