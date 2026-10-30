package com.electorama.geography.service;

import com.electorama.geography.model.dto.SafeHouse;
import com.electorama.geography.model.entity.SafeHouseEntity;
import com.electorama.geography.model.mapper.SafeHouseMapper;
import com.electorama.geography.model.repository.SafeHouseRepository;
import com.electorama.geography.model.rest.UserMunicipalityResponse;
import com.electorama.geography.service.rest.UsersRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SafeHouseServiceImpl implements SafeHouseService {

  @Autowired private UsersRestClient usuariosRestClient;

  @Autowired private SafeHouseRepository safeHouseRepository;

  private final SafeHouseMapper safeHouseMapper = new SafeHouseMapper();

  @Override
  public Optional<SafeHouse> byId(Long id) {

    Optional<SafeHouseEntity> safeHouseEntity = safeHouseRepository.findById(id);
    if (safeHouseEntity.isPresent()) {
      return safeHouseMapper.convertToDtoOptional(safeHouseEntity);
    }

    return Optional.empty();
  }

  @Override
  public List<SafeHouse> findByMunicipalities() {
    List<UserMunicipalityResponse> userMunicipalityResponses =
        usuariosRestClient.readUserMunicipalities();
    List<Long> municipalityIds = new ArrayList<>();

    if (userMunicipalityResponses != null) {
      for (UserMunicipalityResponse userMunicipality : userMunicipalityResponses) {
        String municipalityId = userMunicipality.getMunicipalityId();
        // Ignora municipios sin id (datos sucios en users_municipalities o DTO nulo):
        // antes Long.parseLong(null) lanzaba NumberFormatException -> HTTP 500.
        if (municipalityId != null && !municipalityId.isBlank()) {
          municipalityIds.add(Long.parseLong(municipalityId.trim()));
        }
      }
    }

    // Sin municipios asignados: devuelve lista vacía (evita un IN () y un 500).
    if (municipalityIds.isEmpty()) {
      return new ArrayList<>();
    }

    List<SafeHouseEntity> safeHouseList =
        safeHouseRepository.findByMunicipalityIdIn(municipalityIds);

    return safeHouseMapper.convertToDtoList(safeHouseList);
  }

  @Override
  public SafeHouse save(SafeHouse safeHouse) {
    SafeHouseEntity safeHouseEntityResponse =
        safeHouseRepository.save(safeHouseMapper.convertToEntity(safeHouse));
    return safeHouseMapper.convertToDto(safeHouseEntityResponse);
  }

  @Override
  public SafeHouse delete(Long idSafeHouse) {
    Optional<SafeHouseEntity> safeHouseEntityResponse = safeHouseRepository.findById(idSafeHouse);

    if (safeHouseEntityResponse.isPresent()) {
      SafeHouseEntity safeHouseEntity = safeHouseEntityResponse.get();
      safeHouseRepository.delete(safeHouseEntity);
      return safeHouseMapper.convertToDto(safeHouseEntity);
    }
    throw new RuntimeException("SafeHouse not found");
  }
}
