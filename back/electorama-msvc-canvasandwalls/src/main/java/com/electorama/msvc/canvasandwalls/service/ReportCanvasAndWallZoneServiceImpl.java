package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallZoneDto;
import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallZoneEntity;
import com.electorama.msvc.canvasandwalls.model.mapper.ReportCanvasAndWallsZoneMapper;
import com.electorama.msvc.canvasandwalls.model.repository.ReportCanvasAndWallZoneRepository;
import com.electorama.msvc.canvasandwalls.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.canvasandwalls.model.rest.SeccionResponse;
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
public class ReportCanvasAndWallZoneServiceImpl implements ReportCanvasAndWallZoneService {

  @Autowired ReportCanvasAndWallZoneRepository report;

  @Autowired GeographyRestClient geographyRestClient;

  @Autowired UsersRestClient usuariosRestClient;

  private final ReportCanvasAndWallsZoneMapper reportCanvasAndWallsZoneMapper =
      new ReportCanvasAndWallsZoneMapper();

  @Override
  public List<ReportCanvasAndWallZoneDto> findAllByZone(ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getMunicipality() == 0) {
                return true;
              } else {
                return reportVO
                    .getMunicipality()
                    .toString()
                    .equals(line.getMunicipalityId().toString());
              }
            })
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              otherRow.setSectionId(seccionNumber);
              otherRow.setReferenceName((firstName + ' ' + lastName).trim());
              otherRow.setCoordinateZoneName(
                  (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
              otherRow.setMunicipalityId(nombreMunicipio);
              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByMunicipality(ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .filter(
            line -> {
              if (reportVO.getMunicipality() == 0) {
                return true;
              } else {
                return reportVO
                    .getMunicipality()
                    .toString()
                    .equals(line.getMunicipalityId().toString());
              }
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              otherRow.setSectionId(seccionNumber);
              otherRow.setReferenceName((firstName + ' ' + lastName).trim());
              otherRow.setCoordinateZoneName(
                  (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
              otherRow.setMunicipalityId(nombreMunicipio);
              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByCategory(ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              if (row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())) {
                otherRow.setSectionId(seccionNumber);
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
                otherRow.setCoordinateZoneName(
                    (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
                otherRow.setMunicipalityId(nombreMunicipio);
                return otherRow;
              } else {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByProject(ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              if (row.getCanvasAndWallProyectId().equals(reportVO.getProyect())) {
                otherRow.setSectionId(seccionNumber);
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
                otherRow.setCoordinateZoneName(
                    (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
                otherRow.setMunicipalityId(nombreMunicipio);
                return otherRow;
              } else {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByProjectAndCategory(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              if (row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())
                  && row.getCanvasAndWallProyectId().equals(reportVO.getProyect())) {
                otherRow.setSectionId(seccionNumber);
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
                otherRow.setCoordinateZoneName(
                    (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
                otherRow.setMunicipalityId(nombreMunicipio);
                return otherRow;
              } else {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByCategoryAndMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getMunicipality() == 0) {
                return true;
              } else {
                return reportVO
                    .getMunicipality()
                    .toString()
                    .equals(line.getMunicipalityId().toString());
              }
            })
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              if (row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())) {
                otherRow.setSectionId(seccionNumber);
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
                otherRow.setCoordinateZoneName(
                    (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
                otherRow.setMunicipalityId(nombreMunicipio);
                return otherRow;
              } else {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByProjectAndMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .filter(
            line -> {
              if (reportVO.getMunicipality() == 0) {
                return true;
              } else {
                return reportVO
                    .getMunicipality()
                    .toString()
                    .equals(line.getMunicipalityId().toString());
              }
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              if (row.getCanvasAndWallProyectId().equals(reportVO.getProyect())) {
                otherRow.setSectionId(seccionNumber);
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
                otherRow.setCoordinateZoneName(
                    (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
                otherRow.setMunicipalityId(nombreMunicipio);
                return otherRow;
              } else {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallZoneDto> findByProjectAndCategoryAndMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallZoneEntity> reporte =
        report.findAllByMunicipality(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    return reporte.stream()
        .filter(
            line -> {
              if (reportVO.getCoordinatorZoneAuthId() != null) {
                return reportVO
                    .getCoordinatorZoneAuthId()
                    .equals(line.getCoordinateZoneId().toString());

              } else if (reportVO.getReferenceAuthId() != null) {
                return reportVO
                    .getReferenceAuthId()
                    .toString()
                    .equals(line.getReferenceAuthId().toString());
              }
              return true;
            })
        .filter(
            line -> {
              if (reportVO.getMunicipality() == 0) {
                return true;
              } else {
                return reportVO
                    .getMunicipality()
                    .toString()
                    .equals(line.getMunicipalityId().toString());
              }
            })
        .map(
            row -> {
              ReportCanvasAndWallZoneDto otherRow =
                  reportCanvasAndWallsZoneMapper.convertToDto(row);
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return row.getMunicipalityId()
                                .toString()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              Optional<SeccionResponse> secc =
                  seccionResponse.stream()
                      .filter(
                          seccion ->
                              row.getSectionId()
                                  .equals(Integer.parseInt(seccion.getId().toString())))
                      .findFirst();

              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponseMun.stream()
                      .filter(
                          usrfirstNombre ->
                              row.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          userProfileResponse1 ->
                              row.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
                      .findFirst();

              String firstNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");
              String lastNameCoordinateZone =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              if (row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())
                  && row.getCanvasAndWallProyectId().equals(reportVO.getProyect())) {
                otherRow.setSectionId(seccionNumber);
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
                otherRow.setCoordinateZoneName(
                    (firstNameCoordinateZone + ' ' + lastNameCoordinateZone).trim());
                otherRow.setMunicipalityId(nombreMunicipio);
                return otherRow;
              } else {
                return null;
              }
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }
}
