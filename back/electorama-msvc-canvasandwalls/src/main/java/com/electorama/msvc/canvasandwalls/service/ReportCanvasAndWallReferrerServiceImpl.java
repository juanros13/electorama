package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallReferrerDto;
import com.electorama.msvc.canvasandwalls.model.entity.ReportCanvasAndWallReferrerEntity;
import com.electorama.msvc.canvasandwalls.model.mapper.ReportCanvasAndWallsReferrerMapper;
import com.electorama.msvc.canvasandwalls.model.repository.ReportCanvasAndWallReferrerRepository;
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
public class ReportCanvasAndWallReferrerServiceImpl implements ReportCanvasAndWallReferrerService {

  @Autowired ReportCanvasAndWallReferrerRepository report;

  @Autowired GeographyRestClient geographyRestClient;

  @Autowired UsersRestClient usuariosRestClient;

  private final ReportCanvasAndWallsReferrerMapper reportCanvasAndWallsMunicipalityMapper =
      new ReportCanvasAndWallsReferrerMapper();

  @Override
  public List<ReportCanvasAndWallReferrerDto> findAllByMunicipality(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallReferrerEntity> reporte =
        report.findAllByReferrer(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorMun();

    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallReferrerDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
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

              otherRow.setReferenceName((firstName + ' ' + lastName).trim());
              otherRow.setMunicipalityId(nombreMunicipio);
              return otherRow;
            })
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  @Override
  public List<ReportCanvasAndWallReferrerDto> findByCategory(ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallReferrerEntity> reporte =
        report.findAllByReferrer(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorMun();

    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallReferrerDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
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

              if (row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())) {
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
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
  public List<ReportCanvasAndWallReferrerDto> findByProject(ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallReferrerEntity> reporte =
        report.findAllByReferrer(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorMun();

    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallReferrerDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
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

              if (row.getCanvasAndWallProyectId().equals(reportVO.getProyect())) {
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
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
  public List<ReportCanvasAndWallReferrerDto> findByProjectAndCategory(
      ReportCanvasAndWallDto reportVO) {
    List<ReportCanvasAndWallReferrerEntity> reporte =
        report.findAllByReferrer(reportVO.getStartDate(), reportVO.getEndDate());
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorMun();

    return reporte.stream()
        .map(
            row -> {
              ReportCanvasAndWallReferrerDto otherRow =
                  reportCanvasAndWallsMunicipalityMapper.convertToDto(row);
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

              if (row.getCanvasAndWallProyectId().equals(reportVO.getProyect())
                  && row.getCanvasAndWallCategoryId().equals(reportVO.getCategory())) {
                otherRow.setReferenceName((firstName + ' ' + lastName).trim());
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
