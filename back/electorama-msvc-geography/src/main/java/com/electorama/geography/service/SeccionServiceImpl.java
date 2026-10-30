package com.electorama.geography.service;

import com.electorama.geography.exception.EntityNotFoundException;
import com.electorama.geography.model.dto.Seccion;
import com.electorama.geography.model.entity.SeccionEntity;
import com.electorama.geography.model.mapper.SeccionMapper;
import com.electorama.geography.model.repository.SeccionRepository;
import com.electorama.geography.model.rest.UserMunicipalityResponse;
import com.electorama.geography.service.rest.UsersRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SeccionServiceImpl implements SeccionService {

  @Autowired SeccionRepository seccionRepository;

  @Autowired private UsersRestClient usuariosRestClient;

  private final SeccionMapper seccionMapper = new SeccionMapper();

  @Override
  public Optional<Seccion> byId(Long id) {
    return Optional.of(
        seccionMapper.convertToDto(
            seccionRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public List<Seccion> findByEntityId(Integer id) {
    return new ArrayList<>(seccionMapper.convertToDto(seccionRepository.findByEntidadId(id)));
  }

  @Override
  public List<Seccion> findByFederalDistrictId(Integer id) {
    return new ArrayList<>(
        seccionMapper.convertToDto(seccionRepository.findByDistritoFederalId(id)));
  }

  @Override
  public List<Seccion> findBySectionsByFederalDistrict(Integer federalDistrictId) {
    return new ArrayList<>(
        seccionMapper.convertToDto(
            seccionRepository.findByEntidadIdAndDistritoFedOrderBySeccionAsc(
                27, federalDistrictId)));
  }

  @Override
  public List<Seccion> findByLocalDistrictId(Integer id) {
    return new ArrayList<>(seccionMapper.convertToDto(seccionRepository.findByDistritoLocalId(id)));
  }

  @Override
  public List<Seccion> findSectionByLocalDistrictId(Integer LocalDistrictId) {
    return new ArrayList<>(
        seccionMapper.convertToDto(
            seccionRepository.findByEntidadIdAndDistritoLocOrderBySeccionAsc(27, LocalDistrictId)));
  }

  @Override
  public List<Seccion> findByMunicipality(Integer id) {
    return new ArrayList<>(
        seccionMapper.convertToDto(seccionRepository.findByMunicipioIdOrderBySeccionAsc(id)));
  }

  @Override
  public List<Seccion> findByMunicipalityReference() {
    List<UserMunicipalityResponse> userMunicipalityResponses =
        usuariosRestClient.readUserMunicipalities();
    List<Long> municipalityIds = new ArrayList<>();

    for (UserMunicipalityResponse userMunicipality : userMunicipalityResponses) {
      municipalityIds.add(Long.parseLong(userMunicipality.getMunicipalityId()));
    }
    return new ArrayList<>(
        seccionMapper.convertToDto(seccionRepository.findByMunicipioIdIn(municipalityIds)));
  }

  @Override
  public List<Seccion> findBySections() {
    return seccionMapper.convertToDtoList(seccionRepository.findAll());
  }

  public Seccion save(Seccion seccion) {

    SeccionEntity seccionEntity = seccionMapper.convertToEntity(seccion);
    return seccionMapper.convertToDto(seccionRepository.save(seccionEntity));
  }
}
