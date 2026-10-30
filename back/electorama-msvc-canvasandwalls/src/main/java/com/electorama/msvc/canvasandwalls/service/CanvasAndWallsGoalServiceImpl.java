package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.CanvasAndWallsGoalDto;
import com.electorama.msvc.canvasandwalls.model.entity.CanvasAndWallsGoalEntity;
import com.electorama.msvc.canvasandwalls.model.mapper.CanvasAndWallsGoalMapper;
import com.electorama.msvc.canvasandwalls.model.repository.CanvasAndWallsGoalRepository;
import com.electorama.msvc.canvasandwalls.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.canvasandwalls.model.rest.UserProfileResponse;
import com.electorama.msvc.canvasandwalls.service.rest.GeographyRestClient;
import com.electorama.msvc.canvasandwalls.service.rest.UsersRestClient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CanvasAndWallsGoalServiceImpl implements CanvasAndWallsGoalService {

  @Autowired GeographyRestClient geographyRestClient;

  @Autowired CanvasAndWallsGoalRepository repository;

  @Autowired UsersRestClient usuariosRestClient;

  private final CanvasAndWallsGoalMapper mapper = new CanvasAndWallsGoalMapper();

  @Override
  public CanvasAndWallsGoalDto save(CanvasAndWallsGoalDto canvasAndWallsGoalDto) {
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    CanvasAndWallsGoalEntity savedValue =
        repository.save(mapper.convertToEntity(canvasAndWallsGoalDto));
    canvasAndWallsGoalDto.setReferenceAuthName(
        userProfileResponse.stream()
                .filter(name -> name.getAuthId().equals(canvasAndWallsGoalDto.getReferenceAuthId()))
                .findFirst()
                .map(UserProfileResponse::getFirstName)
                .orElse("")
            + " "
            + userProfileResponse.stream()
                .filter(name -> name.getAuthId().equals(canvasAndWallsGoalDto.getReferenceAuthId()))
                .findFirst()
                .map(UserProfileResponse::getLastName)
                .orElse(""));

    canvasAndWallsGoalDto.setMunicipalityName(
        municipalitiesResponse.stream()
            .filter(county -> county.getId().equals(canvasAndWallsGoalDto.getMunicipalityId()))
            .findFirst()
            .map(MunicipalitiesResponse::getNomMun)
            .orElse(""));
    canvasAndWallsGoalDto.setId(savedValue.getId());
    return canvasAndWallsGoalDto;
  }

  @Override
  public List<CanvasAndWallsGoalDto> findByReferenceAuthId(String referenceAuthId) {
    List<CanvasAndWallsGoalEntity> query = repository.findByReferenceAuthId(referenceAuthId);
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    return query.stream()
        .map(
            row -> {
              CanvasAndWallsGoalDto otherRow = mapper.convertToDto(row);

              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio ->
                              row.getMunicipalityId()
                                  .toString()
                                  .equals(municipio.getId().toString()))
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();

              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              otherRow.setMunicipalityName(nombreMunicipio);
              otherRow.setReferenceAuthName((firstName + ' ' + lastName).trim());

              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<CanvasAndWallsGoalDto> findByMunicipalityId(Integer municipalityId) {
    List<CanvasAndWallsGoalEntity> query = repository.findByMunicipalityId(municipalityId);
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    return query.stream()
        .map(
            row -> {
              CanvasAndWallsGoalDto otherRow = mapper.convertToDto(row);

              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio ->
                              row.getMunicipalityId()
                                  .toString()
                                  .equals(municipio.getId().toString()))
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();

              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              otherRow.setMunicipalityName(nombreMunicipio);
              otherRow.setReferenceAuthName((firstName + ' ' + lastName).trim());

              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<CanvasAndWallsGoalDto> findByMunicipalityIdAndReferenceAuthId(
      Integer municipalityId, String referenceAuthId) {
    List<CanvasAndWallsGoalEntity> query =
        repository.findByMunicipalityIdAndReferenceAuthId(municipalityId, referenceAuthId);
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    return query.stream()
        .map(
            row -> {
              CanvasAndWallsGoalDto otherRow = mapper.convertToDto(row);

              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio ->
                              row.getMunicipalityId()
                                  .toString()
                                  .equals(municipio.getId().toString()))
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();

              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              otherRow.setMunicipalityName(nombreMunicipio);
              otherRow.setReferenceAuthName((firstName + ' ' + lastName).trim());

              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<CanvasAndWallsGoalDto> findByAll() {
    List<CanvasAndWallsGoalEntity> query = repository.findAll();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    return query.stream()
        .map(
            row -> {
              CanvasAndWallsGoalDto otherRow = mapper.convertToDto(row);

              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio ->
                              row.getMunicipalityId()
                                  .toString()
                                  .equals(municipio.getId().toString()))
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();

              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              otherRow.setMunicipalityName(nombreMunicipio);
              otherRow.setReferenceAuthName((firstName + ' ' + lastName).trim());

              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
