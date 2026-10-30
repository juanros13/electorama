package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.model.entity.FollowerMarkEntity;
import com.electorama.msvc.followers.model.repository.FollowersMarkRepository;
import com.electorama.msvc.followers.model.repository.FollowersRepository;
import com.electorama.msvc.followers.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.followers.model.rest.SeccionResponse;
import com.electorama.msvc.followers.service.rest.GeographyRestClient;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MobilizationServiceImpl implements MobilizationService {

  @Autowired private FollowersRepository followersRepository;
  @Autowired private FollowersMarkRepository followersMarkRepository;
  @Autowired private GeographyRestClient geographyRestClient;

  @Autowired private UsersRestClient usuariosRestClient;

  @Override
  public StateMobilizationDto getStateMobilization() {
    StateMobilizationDto result = new StateMobilizationDto();

    List<Object[]> objects = followersRepository.getStateMobilization();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long progressMobilization = ((Number) objArray[0]).longValue();
      long goalMobilization = ((Number) objArray[1]).longValue();

      result.setId(Long.valueOf(idCounter++));
      result.setProgress(progressMobilization);
      result.setGoal(goalMobilization);
    }
    return result;
  }

  @Override
  public List<MunicipalityMobilizationDto> getMunicipalityMobilization() {
    List<MunicipalityMobilizationDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();

    List<Object[]> objects = followersRepository.getMunicipalityMobilization();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();

      String municipalityName = null;
      for (MunicipalitiesResponse municipality : municipalitiesResponses) {
        if (municipality.getId() == municipalityId) {
          municipalityName = municipality.getNomMun();
          break;
        }
      }

      MunicipalityMobilizationDto dto = new MunicipalityMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<MunicipalityByReferentMobilizationDto> getMunicipalityByReferentMobilization(
      String authIdHeader) {
    List<MunicipalityByReferentMobilizationDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();

    List<Object[]> objects = followersRepository.getMobilizationProgressByReferent();
    List<String> idsReferent = new ArrayList<>();
    for (Object[] objArray : objects) {
      idsReferent.add(objArray[3].toString());
    }

    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();
      String ReferentMobilization = objArray[3].toString();

      String municipalityName = null;
      for (MunicipalitiesResponse municipality : municipalitiesResponses) {
        if (municipality.getId() == municipalityId) {
          municipalityName = municipality.getNomMun();
          break;
        }
      }

      String name = null;
      for (UserNamesListDto user : namesReferent) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(ReferentMobilization)) {
            name = user.getNombre();
            break;
          }
        }
      }

      MunicipalityByReferentMobilizationDto dto = new MunicipalityByReferentMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setReferentId(ReferentMobilization);
      dto.setReferentName(name);
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<ResultFederalDistrictDto> getFederalDistrictByReferentMobilization() {
    List<ResultFederalDistrictDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();
    List<Object[]> objects = followersRepository.getFederalDistrictByReferentMobilization();
    List<String> idsReferent = new ArrayList<>();

    // Mapa para mantener la sumatoria de goal y progress por ReferentId, municipalityId y
    // federalDistrict
    Map<String, Map<Long, Map<Long, Long>>> sumMap = new HashMap<>();

    // Recorre los objetos de la consulta para calcular la sumatoria de goal y progress
    for (Object[] objArray : objects) {
      String referentId = objArray[3].toString();
      Long municipalityId = ((Number) objArray[0]).longValue();
      Long progressMobilization = ((Number) objArray[1]).longValue();
      Long goalMobilization = ((Number) objArray[2]).longValue();

      // Agrega el ReferentId a la lista de idsReferent
      idsReferent.add(referentId);

      // Calcula la sumatoria de goal y progress para cada grupo
      Map<Long, Map<Long, Long>> municipalityMap =
          sumMap.computeIfAbsent(referentId, k -> new HashMap<>());
      Map<Long, Long> districtMap =
          municipalityMap.computeIfAbsent(municipalityId, k -> new HashMap<>());
      districtMap.put(1L, districtMap.getOrDefault(1L, 0L) + goalMobilization);
      districtMap.put(2L, districtMap.getOrDefault(2L, 0L) + progressMobilization);
    }

    // Obtiene los nombres de los referentes
    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

    int idCounter = 1;
    for (String referentId : sumMap.keySet()) {
      Map<Long, Map<Long, Long>> municipalityMap = sumMap.get(referentId);
      for (Long municipalityId : municipalityMap.keySet()) {
        Long goal = municipalityMap.get(municipalityId).get(1L);
        Long progress = municipalityMap.get(municipalityId).get(2L);
        String municipalityName = null;
        Long federalDistrict = null;

        // Obtiene el nombre del municipio y el distrito federal
        for (MunicipalitiesResponse municipality : municipalitiesResponses) {
          if (municipality.getId().equals(municipalityId)) {
            municipalityName = municipality.getNomMun();
            break;
          }
        }
        for (SeccionResponse seccion : seccionResponse) {
          if (seccion.getMunicipioId().equals(municipalityId)) {
            federalDistrict = seccion.getDistritoFed();
            break;
          }
        }

        // Obtiene el nombre del referente
        String referentName = null;
        for (UserNamesListDto user : namesReferent) {
          if (user.getAuthId().equalsIgnoreCase(referentId)) {
            referentName = user.getNombre();
            break;
          }
        }

        // Crea y agrega el objeto FederalDistrictByReferentMobilizationDto a la lista de resultados
        ResultFederalDistrictDto dto = new ResultFederalDistrictDto();
        dto.setId(Long.valueOf(idCounter++));
        dto.setReferentId(referentId);
        dto.setReferentName(referentName);
        dto.setMunicipalityId(municipalityId);
        dto.setMunicipality(municipalityName);
        dto.setGoal(goal);
        dto.setProgress(progress);
        dto.setFederalDistrict(federalDistrict);
        result.add(dto);
      }
    }

    return result;
  }

  @Override
  public List<SectionByMunicipalityMobilizationDto> getMobilizationSectionByMunicipality() {
    List<SectionByMunicipalityMobilizationDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();

    List<Object[]> objects = followersRepository.getMobilizationSectionByMunicipality();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      String sectionNum = objArray[1].toString();
      long progressMobilization = ((Number) objArray[2]).longValue();
      long goalMobilization = ((Number) objArray[3]).longValue();

      String municipalityName = null;
      for (MunicipalitiesResponse municipality : municipalitiesResponses) {
        if (municipality.getId() == municipalityId) {
          municipalityName = municipality.getNomMun();
          break;
        }
      }

      SectionByMunicipalityMobilizationDto dto = new SectionByMunicipalityMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setSectionNumber(sectionNum);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<MunicipalityByReferentAndCoordinatorMobilizationDto>
      getMunicipalityByReferentAndCoordinatorMobilization(
          HttpServletRequest servletRequest,
          String authIdHeader,
          Integer idMunicipality,
          String referentId,
          Integer federalDistrict,
          Integer localDistrict) {
    String roleName = getRoleName(servletRequest);

    List<MunicipalityByReferentAndCoordinatorMobilizationDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();

    List<Object[]> objects = new ArrayList<>();
    if (roleName.equalsIgnoreCase("ADMIN")) {
      log.info("es admin");
      if (idMunicipality != null && idMunicipality > 0) {
        log.info("x municipio");
        objects = followersRepository.getMunicipality(idMunicipality);
      } else if (referentId != null && !referentId.isEmpty()) {
        log.info("x referent");
        objects = followersRepository.getReferent(referentId);
      } else if (federalDistrict != null && federalDistrict > 0) {
        log.info("x federal district");
        objects = followersRepository.getFederalDistrict(federalDistrict.toString());
      } else if (localDistrict != null && localDistrict > 0) {
        log.info("x local district");
        objects = followersRepository.getLocalDistrict(localDistrict.toString());
      } else {
        log.info("es admin");
        objects = followersRepository.getMunicipalityByReferentAndCoordinator();
      }

    } else if (roleName.equalsIgnoreCase("CORDINATOR_MUNICIPAL")) {
      log.info("es referente");
      objects = followersRepository.getMunicipalityByReferentAndCoordinator(authIdHeader);
    } else if (roleName.equalsIgnoreCase("CORDINATOR_MUNICIPAL_ALT")) {
      log.info("es referente");
      objects = followersRepository.getMunicipalityByReferentAndCoordinatorAlt(authIdHeader);
    }

    List<String> idsReferent = new ArrayList<>();
    List<String> idsCordinator = new ArrayList<>();
    for (Object[] objArray : objects) {
      idsReferent.add(objArray[3].toString());
    }
    Set<String> setWithoutDuplicates = new HashSet<>(idsReferent);
    List<String> uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(uniqueList);

    for (Object[] objArray : objects) {
      idsCordinator.add(objArray[4].toString());
    }
    setWithoutDuplicates = new HashSet<>(idsCordinator);
    uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesCordinator = usuariosRestClient.getUsersList(uniqueList);

    int idCounter = 1;
    String fedDist = null;
    String locDist = null;

    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();
      String ReferentMobilization = objArray[3].toString();
      String CoordinatorMobilization = objArray[4].toString();
      Long seccionId = ((Number) objArray[5] != null ? ((Number) objArray[5]).longValue() : 0);
      String seccionnNumber = objArray[6].toString();

      if (federalDistrict != null && federalDistrict > 0) {
        fedDist = objArray[7] != null ? objArray[7].toString() : "";
      }
      if (localDistrict != null && localDistrict > 0) {
        locDist = objArray[7] != null ? objArray[7].toString() : "";
      }

      String municipalityName = null;
      for (MunicipalitiesResponse municipality : municipalitiesResponses) {
        if (municipality.getId() == municipalityId) {
          municipalityName = municipality.getNomMun();
          break;
        }
      }

      String name = null;
      String coordinatorName = null;
      for (UserNamesListDto user : namesReferent) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(ReferentMobilization)) {
            name = user.getNombre();
          }
        }
      }

      for (UserNamesListDto user : namesCordinator) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(CoordinatorMobilization)) {
            coordinatorName = user.getNombre();
          }
        }
      }

      MunicipalityByReferentAndCoordinatorMobilizationDto dto =
          new MunicipalityByReferentAndCoordinatorMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setReferentId(ReferentMobilization);
      dto.setReferentName(name);
      dto.setCoordinatorId(CoordinatorMobilization);
      dto.setCoordinatorName(coordinatorName);
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);
      dto.setSeccionId(seccionId);
      dto.setSeccionNumber(seccionnNumber);
      dto.setFederaDistrictName(fedDist);
      dto.setLocalDistrictName(locDist);

      result.add(dto);
    }
    return result;
  }

  public boolean updateIsSyncronizedById(Long id, boolean markFlag, boolean isSinchronized) {
    FollowerMarkEntity follower =
        followersMarkRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new MobilizationServiceImpl.FollowerNotFoundException(
                        "Follower no encontrado con el ID: " + id));
    followersMarkRepository.updateIsSyncronizedById(id, markFlag, isSinchronized);
    return markFlag;
  }

  public boolean updateVoteFlagAndSyncronizedList(UpdateVoteFlagAndSyncronizedListDto request) {
    boolean allUpdated = true;
    for (Long id : request.getIds()) {
      boolean updated =
          updateIsSyncronizedById(id, request.getMarkFlag(), request.getIsSynchronized());
      if (!updated) {
        allUpdated = false;
      }
    }
    return allUpdated;
  }

  @Override
  public List<MunicipalityBySectionAndReferentMobilizationDto>
      getMunicipalityBySectionAndReferentMobilization(Long municipalityId) {
    List<MunicipalityBySectionAndReferentMobilizationDto> result = new ArrayList<>();

    List<Object[]> objects =
        followersRepository.getMunicipalityBySectionAndReferentMobilization(municipalityId);
    List<String> idsReferent = new ArrayList<>();
    Set<String> idsSet = new HashSet<>();

    for (Object[] objArray : objects) {
      String id = objArray[4].toString();
      if (idsSet.add(id)) { // add returns false if the element is already in the set
        idsReferent.add(id);
      }
    }
    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();
      String sectionNum = objArray[3].toString();
      String ReferentMobilization = objArray[4].toString();
      Long sectionId = ((Number) objArray[5]).longValue();
      String municipalityName = objArray[7].toString();
      Long federalDistrictId = ((Number) objArray[8]).longValue();
      String federalDistrictName = objArray[9].toString();
      Long localDistrictId = ((Number) objArray[10]).longValue();
      String localDistrictName = objArray[11].toString();

      String name = null;
      for (UserNamesListDto user : namesReferent) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(ReferentMobilization)) {
            name = user.getNombre();
            break;
          }
        }
      }

      MunicipalityBySectionAndReferentMobilizationDto dto =
          new MunicipalityBySectionAndReferentMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setReferentId(ReferentMobilization);
      dto.setReferentName(name);
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);
      dto.setSectionNumber(sectionNum);
      dto.setSectionId(sectionId);
      dto.setFederalDistrictId(federalDistrictId);
      dto.setFederalDistrictName(federalDistrictName);
      dto.setLocalDistrictId(localDistrictId);
      dto.setLocalDistrictName(localDistrictName);

      result.add(dto);
    }
    return result;
  }

  public List<FederalDistrictDto> getFederalDistrictMobilization() {
    List<FederalDistrictDto> result = new ArrayList<>();

    List<Object[]> objects = followersRepository.getFederalDistrictMobilizationNuevo();

    // Mapa para mantener la sumatoria de goal y progress por ReferentId, municipalityId y
    // federalDistrict
    Map<Long, Map<Long, Long>> sumMap = new HashMap<>();
    Integer idCounter = 1;
    // Recorre los objetos de la consulta para calcular la sumatoria de goal y progress
    for (Object[] objArray : objects) {

      FederalDistrictDto dto = new FederalDistrictDto();
      dto.setId(idCounter.longValue());
      idCounter++;
      dto.setFederalDistrict(Long.parseLong(objArray[0].toString()));
      dto.setProgress(((Number) objArray[1]).longValue());
      dto.setGoal(((Number) objArray[2]).longValue());
      List<String> municipalityNamesList = new ArrayList<>();
      dto.setMunicipalityNames(municipalityNamesList);
      result.add(dto);
    }
    return result;
  }

  public List<LocalDistrictDto> getLocalDistrictMobilization() {
    List<LocalDistrictDto> result = new ArrayList<>();

    List<Object[]> objects = followersRepository.getLocalDistrictMobilizationNuevo();

    // Mapa para mantener la sumatoria de goal y progress por ReferentId, municipalityId y
    // federalDistrict
    Map<Long, Map<Long, Long>> sumMap = new HashMap<>();
    Integer idCounter = 1;
    // Recorre los objetos de la consulta para calcular la sumatoria de goal y progress
    for (Object[] objArray : objects) {

      LocalDistrictDto dto = new LocalDistrictDto();
      dto.setId(idCounter.longValue());
      idCounter++;
      dto.setLocalDistrict(Long.parseLong(objArray[0].toString()));
      dto.setProgress(((Number) objArray[1]).longValue());
      dto.setGoal(((Number) objArray[2]).longValue());
      List<String> municipalityNamesList = new ArrayList<>();
      dto.setMunicipalityNames(municipalityNamesList);
      result.add(dto);
    }
    return result;
  }

  public class FollowerNotFoundException extends RuntimeException {
    public FollowerNotFoundException(String message) {
      super(message);
    }
  }

  @Override
  public List<ResultFederalDistrictDto> getReferentsByFederalDistrictMobilization(
      Long federalDistrictId) {
    List<ResultFederalDistrictDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();
    List<Object[]> objects = followersRepository.getFederalDistrictByReferentMobilization();
    List<String> idsReferent = new ArrayList<>();

    // Mapa para mantener la sumatoria de goal y progress por ReferentId, municipalityId y
    // federalDistrict
    Map<String, Map<Long, Map<Long, Long>>> sumMap = new HashMap<>();

    // Recorre los objetos de la consulta para calcular la sumatoria de goal y progress
    for (Object[] objArray : objects) {
      String referentId = objArray[3].toString();
      Long municipalityId = ((Number) objArray[0]).longValue();
      Long progressMobilization = ((Number) objArray[1]).longValue();
      Long goalMobilization = ((Number) objArray[2]).longValue();

      // Agrega el ReferentId a la lista de idsReferent
      idsReferent.add(referentId);

      // Calcula la sumatoria de goal y progress para cada grupo
      Map<Long, Map<Long, Long>> municipalityMap =
          sumMap.computeIfAbsent(referentId, k -> new HashMap<>());
      Map<Long, Long> districtMap =
          municipalityMap.computeIfAbsent(municipalityId, k -> new HashMap<>());
      districtMap.put(1L, districtMap.getOrDefault(1L, 0L) + goalMobilization);
      districtMap.put(2L, districtMap.getOrDefault(2L, 0L) + progressMobilization);
    }

    // Obtiene los nombres de los referentes
    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

    int idCounter = 1;
    for (String referentId : sumMap.keySet()) {
      Map<Long, Map<Long, Long>> municipalityMap = sumMap.get(referentId);
      for (Long municipalityId : municipalityMap.keySet()) {
        Long goal = municipalityMap.get(municipalityId).get(1L);
        Long progress = municipalityMap.get(municipalityId).get(2L);
        String municipalityName = null;
        Long federalDistrict = null;

        // Obtiene el nombre del municipio y el distrito federal
        for (MunicipalitiesResponse municipality : municipalitiesResponses) {
          if (municipality.getId().equals(municipalityId)) {
            municipalityName = municipality.getNomMun();
            break;
          }
        }
        for (SeccionResponse seccion : seccionResponse) {
          if (seccion.getMunicipioId().equals(municipalityId)) {
            federalDistrict = seccion.getDistritoFed();
            break;
          }
        }

        // Si el distrito federal coincide con el proporcionado, agrega los datos a result
        if (federalDistrict != null && federalDistrict.equals(federalDistrictId)) {
          // Obtiene el nombre del referente
          String referentName = null;
          for (UserNamesListDto user : namesReferent) {
            if (user.getAuthId().equalsIgnoreCase(referentId)) {
              referentName = user.getNombre();
              break;
            }
          }

          // Crea y agrega el objeto ResultFederalDistrictDto a la lista de resultados
          ResultFederalDistrictDto dto = new ResultFederalDistrictDto();
          dto.setId(Long.valueOf(idCounter++));
          dto.setReferentId(referentId);
          dto.setReferentName(referentName);
          dto.setMunicipalityId(municipalityId);
          dto.setMunicipality(municipalityName);
          dto.setGoal(goal);
          dto.setProgress(progress);
          dto.setFederalDistrict(federalDistrict);
          result.add(dto);
        }
      }
    }

    return result;
  }

  @Override
  public List<ResultLocalDistrictDto> getReferentsByLocalDistrictMobilization(
      Long localDistrictId) {
    List<ResultLocalDistrictDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();
    List<Object[]> objects = followersRepository.getFederalDistrictByReferentMobilization();
    List<String> idsReferent = new ArrayList<>();

    // Mapa para mantener la sumatoria de goal y progress por ReferentId, municipalityId y
    // federalDistrict
    Map<String, Map<Long, Map<Long, Long>>> sumMap = new HashMap<>();

    // Recorre los objetos de la consulta para calcular la sumatoria de goal y progress
    for (Object[] objArray : objects) {
      String referentId = objArray[3].toString();
      Long municipalityId = ((Number) objArray[0]).longValue();
      Long progressMobilization = ((Number) objArray[1]).longValue();
      Long goalMobilization = ((Number) objArray[2]).longValue();

      // Agrega el ReferentId a la lista de idsReferent
      idsReferent.add(referentId);

      // Calcula la sumatoria de goal y progress para cada grupo
      Map<Long, Map<Long, Long>> municipalityMap =
          sumMap.computeIfAbsent(referentId, k -> new HashMap<>());
      Map<Long, Long> districtMap =
          municipalityMap.computeIfAbsent(municipalityId, k -> new HashMap<>());
      districtMap.put(1L, districtMap.getOrDefault(1L, 0L) + goalMobilization);
      districtMap.put(2L, districtMap.getOrDefault(2L, 0L) + progressMobilization);
    }

    // Obtiene los nombres de los referentes
    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

    int idCounter = 1;
    for (String referentId : sumMap.keySet()) {
      Map<Long, Map<Long, Long>> municipalityMap = sumMap.get(referentId);
      for (Long municipalityId : municipalityMap.keySet()) {
        Long goal = municipalityMap.get(municipalityId).get(1L);
        Long progress = municipalityMap.get(municipalityId).get(2L);
        String municipalityName = null;
        Long localDistrict = null;

        // Obtiene el nombre del municipio y el distrito federal
        for (MunicipalitiesResponse municipality : municipalitiesResponses) {
          if (municipality.getId().equals(municipalityId)) {
            municipalityName = municipality.getNomMun();
            break;
          }
        }
        for (SeccionResponse seccion : seccionResponse) {
          if (seccion.getMunicipioId().equals(municipalityId)) {
            localDistrict = seccion.getDistritoLoc();
            break;
          }
        }

        // Si el distrito federal coincide con el proporcionado, agrega los datos a result
        if (localDistrict != null && localDistrict.equals(localDistrictId)) {
          // Obtiene el nombre del referente
          String referentName = null;
          for (UserNamesListDto user : namesReferent) {
            if (user.getAuthId().equalsIgnoreCase(referentId)) {
              referentName = user.getNombre();
              break;
            }
          }

          // Crea y agrega el objeto ResultFederalDistrictDto a la lista de resultados
          ResultLocalDistrictDto dto = new ResultLocalDistrictDto();
          dto.setId(Long.valueOf(idCounter++));
          dto.setReferentId(referentId);
          dto.setReferentName(referentName);
          dto.setMunicipalityId(municipalityId);
          dto.setMunicipality(municipalityName);
          dto.setGoal(goal);
          dto.setProgress(progress);
          dto.setLocalDistrict(localDistrictId);
          result.add(dto);
        }
      }
    }

    return result;
  }

  public String getRoleName(HttpServletRequest servletRequest) {
    Map<String, String> roleMap = new HashMap<>();
    roleMap.put("ADMIN", "ADMIN");
    roleMap.put("MUNICIPALITY_TRACKER", "MUNICIPALITY_TRACKER");
    roleMap.put("LOCAL_DISTRICT_TRACKER", "LOCAL_DISTRICT_TRACKER");
    roleMap.put("FEDERAL_DISTRICT_TRACKER", "FEDERAL_DISTRICT_TRACKER");
    roleMap.put("CORDINATOR_MUNICIPAL", "CORDINATOR_MUNICIPAL");
    roleMap.put("CORDINATOR_MUNICIPAL_ALT", "CORDINATOR_MUNICIPAL_ALT");

    for (Map.Entry<String, String> entry : roleMap.entrySet()) {
      if (servletRequest.isUserInRole(entry.getKey())) {
        return entry.getValue();
      }
    }
    return "Unknown Role"; // Devuelve un valor por defecto si no se encuentra ningún rol
  }

  @Override
  public List<MunicipalityByReferentAndCoordinatorMobilizationDto> getReferentAlt(
      HttpServletRequest servletRequest, String authIdHeader) {
    String roleName = getRoleName(servletRequest);

    List<MunicipalityByReferentAndCoordinatorMobilizationDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();

    List<Object[]> objects = new ArrayList<>();
    if (roleName.equalsIgnoreCase("ADMIN")) {
      log.info("es admin");
      objects = followersRepository.getReferentAltAdm();

    } else if (roleName.equalsIgnoreCase("CORDINATOR_MUNICIPAL_ALT")) {
      log.info("es referente alterno");
      objects = followersRepository.getReferentAlt(authIdHeader);
    }

    List<String> idsReferent = new ArrayList<>();
    List<String> idsCordinator = new ArrayList<>();
    for (Object[] objArray : objects) {
      idsReferent.add(objArray[3].toString());
    }
    Set<String> setWithoutDuplicates = new HashSet<>(idsReferent);
    List<String> uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(uniqueList);

    for (Object[] objArray : objects) {
      idsCordinator.add(objArray[4].toString());
    }
    setWithoutDuplicates = new HashSet<>(idsCordinator);
    uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesCordinator = usuariosRestClient.getUsersList(uniqueList);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();
      String ReferentMobilization = objArray[3].toString();
      String CoordinatorMobilization = objArray[4].toString();
      Long seccionId = ((Number) objArray[5] != null ? ((Number) objArray[5]).longValue() : 0);
      String seccionnNumber = objArray[6].toString();

      String municipalityName = null;
      for (MunicipalitiesResponse municipality : municipalitiesResponses) {
        if (municipality.getId() == municipalityId) {
          municipalityName = municipality.getNomMun();
          break;
        }
      }

      String name = null;
      String coordinatorName = null;
      for (UserNamesListDto user : namesReferent) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(ReferentMobilization)) {
            name = user.getNombre();
          }
        }
      }

      for (UserNamesListDto user : namesCordinator) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(CoordinatorMobilization)) {
            coordinatorName = user.getNombre();
          }
        }
      }

      MunicipalityByReferentAndCoordinatorMobilizationDto dto =
          new MunicipalityByReferentAndCoordinatorMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setReferentId(ReferentMobilization);
      dto.setReferentName(name);
      dto.setCoordinatorId(CoordinatorMobilization);
      dto.setCoordinatorName(coordinatorName);
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);
      dto.setSeccionId(seccionId);
      dto.setSeccionNumber(seccionnNumber);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<MobilizationAltDto> getMobilizationAlt(
      HttpServletRequest servletRequest, String authIdHeader) {
    String roleName = getRoleName(servletRequest);
    List<MobilizationAltDto> result = new ArrayList<>();

    List<Object[]> objects = null;

    if (roleName.equalsIgnoreCase("ADMIN")) {
      objects = followersRepository.getMobilizationAltAdm();
    } else if (roleName.equalsIgnoreCase("CORDINATOR_MUNICIPAL_ALT")) {
      objects = followersRepository.getMobilizationAlt(authIdHeader);
    }

    List<String> idsReferent = new ArrayList<>();

    for (Object[] objArray : objects) {
      idsReferent.add(objArray[0].toString());
    }

    Set<String> setWithoutDuplicates = new HashSet<>(idsReferent);
    List<String> uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(uniqueList);

    for (Object[] objArray : objects) {
      String referent = objArray[0].toString();
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();

      String name = null;
      for (UserNamesListDto user : namesReferent) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(referent)) {
            name = user.getNombre();
            break;
          }
        }
      }

      MobilizationAltDto dto = new MobilizationAltDto();
      dto.setReferentAuthId(referent);
      dto.setReferentName(name);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);

      result.add(dto);
    }
    return result;
  }

  @Override
  public List<MunicipalityByReferentAndCoordinatorMobilizationDto>
      getMunicipalityByReferentAltAndCoordinatorMobilization(
          HttpServletRequest servletRequest, String authIdHeader, String referentAltId) {
    String roleName = getRoleName(servletRequest);

    List<MunicipalityByReferentAndCoordinatorMobilizationDto> result = new ArrayList<>();

    List<MunicipalitiesResponse> municipalitiesResponses =
        geographyRestClient.readMunicipalitiesByEntity();

    List<Object[]> objects = new ArrayList<>();
    if (roleName.equalsIgnoreCase("ADMIN")) {

      if (referentAltId != null && !referentAltId.isEmpty()) {
        log.info("x referent alt");
        objects = followersRepository.getReferentAlt2(referentAltId);
      } else {
        log.info("es admin");
        objects = followersRepository.getMunicipalityByReferentAndCoordinator();
      }
    }

    List<String> idsReferent = new ArrayList<>();
    List<String> idsCordinator = new ArrayList<>();
    for (Object[] objArray : objects) {
      idsReferent.add(objArray[3].toString());
    }
    Set<String> setWithoutDuplicates = new HashSet<>(idsReferent);
    List<String> uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(uniqueList);

    for (Object[] objArray : objects) {
      idsCordinator.add(objArray[4].toString());
    }
    setWithoutDuplicates = new HashSet<>(idsCordinator);
    uniqueList = new ArrayList<>(setWithoutDuplicates);

    List<UserNamesListDto> namesCordinator = usuariosRestClient.getUsersList(uniqueList);

    int idCounter = 1;
    String fedDist = null;
    String locDist = null;

    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      long progressMobilization = ((Number) objArray[1]).longValue();
      long goalMobilization = ((Number) objArray[2]).longValue();
      String ReferentMobilization = objArray[3].toString();
      String CoordinatorMobilization = objArray[4].toString();
      Long seccionId = ((Number) objArray[5] != null ? ((Number) objArray[5]).longValue() : 0);
      String seccionnNumber = objArray[6].toString();

      String municipalityName = null;
      for (MunicipalitiesResponse municipality : municipalitiesResponses) {
        if (municipality.getId() == municipalityId) {
          municipalityName = municipality.getNomMun();
          break;
        }
      }

      String name = null;
      String coordinatorName = null;
      for (UserNamesListDto user : namesReferent) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(ReferentMobilization)) {
            name = user.getNombre();
          }
        }
      }

      for (UserNamesListDto user : namesCordinator) {
        if (user.getAuthId() != null) {
          if (user.getAuthId().equalsIgnoreCase(CoordinatorMobilization)) {
            coordinatorName = user.getNombre();
          }
        }
      }

      MunicipalityByReferentAndCoordinatorMobilizationDto dto =
          new MunicipalityByReferentAndCoordinatorMobilizationDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setReferentId(ReferentMobilization);
      dto.setReferentName(name);
      dto.setCoordinatorId(CoordinatorMobilization);
      dto.setCoordinatorName(coordinatorName);
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipality(municipalityName);
      dto.setProgress(progressMobilization);
      dto.setGoal(goalMobilization);
      dto.setSeccionId(seccionId);
      dto.setSeccionNumber(seccionnNumber);
      dto.setFederaDistrictName(fedDist);
      dto.setLocalDistrictName(locDist);

      result.add(dto);
    }
    return result;
  }
}
