package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.facade.*;
import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.model.rest.*;
import com.electorama.msvc.followers.service.rest.GeographyRestClient;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportFollowersServiceImpl implements ReportFollowersService {

  @Autowired ReportFollowersByCoordinatorZoneFacade findReportFollowersByCoordinatorZone;

  @Autowired ReportFollowersByReferentFacade reportFollowersByReferentFacade;

  @Autowired ReportFollowersByMunicipalityFacade reportFollowersByMunicipality;

  @Autowired DownloadFollowersFacade downloadFollowersFacade;
  @Autowired UsersRestClient usuariosRestClient;

  @Autowired ReportFollowersMobilizationBySectionFacade reportFollowersMobilizationBySectionFacade;

  @Autowired GeographyRestClient geographyRestClient;

  @Override
  public List<ReportFollowersByCoordinatorZoneDto> findReportFollowersByCoordinatorZone(
      DatesRequestDto fechasDto, String typeRol, Integer municipalityId, String authIdHeader) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";

    List<ReportFollowersByCoordinatorZoneDto> reporte =
        findReportFollowersByCoordinatorZone.findReportFollowersByCoordinatorZone(
            dateStart, dateEnd, typeRol, authIdHeader);

    List<UserCoordinateResponse> userCoordinateResponse =
        usuariosRestClient.readMetaCoordinateZone();

    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseCoordMun = usuariosRestClient.readUserAllCoorMun();

    if (typeRol == "CORDINATOR_ZONE") {
      userCoordinateResponse =
          userCoordinateResponse.stream()
              .filter(
                  user -> {
                    if (user.getCoordinateZoneId().equals(authIdHeader)) {
                      return true;
                    }
                    return false;
                  })
              .collect(Collectors.toList());
    }

    if (typeRol == "CORDINATOR_MUNICIPAL") {
      userCoordinateResponse =
          userCoordinateResponse.stream()
              .filter(
                  user -> {
                    if (user.getReferenceAuthId().equals(authIdHeader)) {
                      return true;
                    }
                    return false;
                  })
              .collect(Collectors.toList());
    }
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();
    List<ReportFollowersByCoordinatorZoneDto> response = new ArrayList<>();

    for (UserCoordinateResponse userMeta : userCoordinateResponse) {
      ReportFollowersByCoordinatorZoneDto reponseReport = new ReportFollowersByCoordinatorZoneDto();

      reponseReport.setNumFollowers(0);
      reponseReport.setNumPromoters(0);
      reporte.stream()
          .map(
              rep -> {
                if (userMeta.getCoordinateZoneId().equals(rep.getCordinatorZoneAuthId())
                    && userMeta.getSeccionId().equals(rep.getSeccionId())) {
                  reponseReport.setId(rep.getId());
                  reponseReport.setMunicipalityId(rep.getMunicipalityId());
                  reponseReport.setReferentAuthId(rep.getReferentAuthId());
                  reponseReport.setNombreCoordinateZone(rep.getNombreCoordinateZone());
                  reponseReport.setSeccionId(rep.getSeccionId());
                  reponseReport.setSeccionNumber(rep.getSeccionNumber());
                  reponseReport.setCordinatorZoneAuthId(rep.getCordinatorZoneAuthId());
                  reponseReport.setNombreCoordinateZone(rep.getNombreCoordinateZone());
                  reponseReport.setNomMun(rep.getNomMun());
                  reponseReport.setNumFollowers(
                      rep.getNumFollowers() != null ? rep.getNumFollowers() : 0);
                  reponseReport.setGoal(rep.getGoal() != null ? rep.getGoal() : 0);
                  reponseReport.setNumPromoters(
                      rep.getNumPromoters() != null ? rep.getNumPromoters() : 0);
                }
                return rep;
              })
          .collect(Collectors.toList());

      // ReportVisitsByCoordinatorZoneSectionRefDto reponseReport = report.get();

      reponseReport.setCordinatorZoneAuthId(userMeta.getCoordinateZoneId());
      reponseReport.setReferentAuthId(userMeta.getReferenceAuthId());
      reponseReport.setMunicipalityId(userMeta.getMunicipalityId());
      reponseReport.setSeccionId(userMeta.getSeccionId());

      if (reponseReport.getMunicipalityId() != null) {
        Optional<MunicipalitiesResponse> muni =
            municipalitiesResponse.stream()
                .filter(
                    municipio -> {
                      return reponseReport
                          .getMunicipalityId()
                          .equals(Integer.parseInt(municipio.getId().toString()));
                    })
                .findFirst();
        String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
        reponseReport.setNomMun(nombreMunicipio);
      }

      if (reponseReport.getSeccionId() != null) {
        Optional<SeccionResponse> secc =
            seccionResponse.stream()
                .filter(
                    seccion -> {
                      return reponseReport
                          .getSeccionId()
                          .equals(Integer.parseInt(seccion.getId().toString()));
                    })
                .findFirst();

        Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
        reponseReport.setSeccionNumber(seccionNumber);
      }
      if (reponseReport.getReferentAuthId() != null) {
        Optional<UserProfileResponse> useProfileOptional =
            userProfileResponseCoordMun.stream()
                .filter(
                    usrfirstNombre ->
                        reponseReport.getReferentAuthId().equals(usrfirstNombre.getAuthId()))
                .findFirst();

        String firstName = useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
        String lastName = useProfileOptional.map(UserProfileResponse::getLastName).orElse("");

        reponseReport.setNombreReferenceAuth(firstName + " " + lastName);
      }
      // Obtener los nombres de los coordinadores de zona
      Optional<UserProfileResponse> useProfileCoorOptional =
          userProfileResponse.stream()
              .filter(
                  userProfileResponse1 ->
                      reponseReport
                          .getCordinatorZoneAuthId()
                          .equals(userProfileResponse1.getAuthId()))
              .findFirst();

      String firstNameCoor =
          useProfileCoorOptional.map(UserProfileResponse::getFirstName).orElse("");
      String lastNameCoor = useProfileCoorOptional.map(UserProfileResponse::getLastName).orElse("");

      reponseReport.setNombreCoordinateZone(firstNameCoor + " " + lastNameCoor);

      response.add(reponseReport);
    }
    if (municipalityId != 0) {
      response =
          response.stream()
              .filter(
                  resp -> {
                    if (resp.getMunicipalityId().equals(municipalityId)) {
                      return true;
                    }
                    return false;
                  })
              .collect(Collectors.toList());
    }

    return response;
  }

  @Override
  public List<ReportFollowersByReferentDto> reportFollowersByReferent(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";

    List<UserCoordinateResponse> userCoordinateResponse =
        usuariosRestClient.readMetaCoordinateZone();

    if (typeRol == "CORDINATOR_ZONE") {
      userCoordinateResponse =
          userCoordinateResponse.stream()
              .filter(
                  user -> {
                    if (user.getCoordinateZoneId().equals(authIdHeader)) {
                      return true;
                    }
                    return false;
                  })
              .collect(Collectors.toList());
    }

    if (typeRol == "CORDINATOR_MUNICIPAL") {
      userCoordinateResponse =
          userCoordinateResponse.stream()
              .filter(
                  user -> {
                    if (user.getReferenceAuthId().equals(authIdHeader)) {
                      return true;
                    }
                    return false;
                  })
              .collect(Collectors.toList());
    }

    List<ReportFollowersByReferentDto> reporte =
        reportFollowersByReferentFacade.reportFollowersByReferent(
            dateStart, dateEnd, typeRol, authIdHeader);

    List<UserResponse> userResponse = usuariosRestClient.readMetaMunicipalities();

    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();

    reporte.stream()
        .map(
            reporteTmp -> {
              Optional<UserResponse> userResponse1 =
                  userResponse.stream()
                      .filter(
                          usr -> {
                            return reporteTmp
                                .getMunicipalityId()
                                .equals(Integer.parseInt(usr.getMunicipalityId().toString()));
                          })
                      .findFirst();

              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return reporteTmp
                                .getMunicipalityId()
                                .equals(Integer.parseInt(municipio.getId().toString()));
                          })
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

              reporteTmp.setNomMun(nombreMunicipio);

              if (reporteTmp.getReferentAuthId() != null) {
                Optional<UserProfileResponse> useProfileOptional =
                    userProfileResponse.stream()
                        .filter(
                            usrfirstNombre ->
                                reporteTmp.getReferentAuthId().equals(usrfirstNombre.getAuthId()))
                        .findFirst();

                String firstName =
                    useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
                String lastName =
                    useProfileOptional.map(UserProfileResponse::getLastName).orElse("");

                reporteTmp.setNombreReferenceAuth(firstName + " " + lastName);
              }

              return reporteTmp;
            })
        .collect(Collectors.toList());

    return reporte;
  }

  @Override
  public List<ReportFollowersByMunicipalityDto> reportFollowersByMunicipality(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";

    List<UserResponse> userResponse = usuariosRestClient.readMetaMunicipalities();

    List<ReportFollowersByMunicipalityDto> reporte =
        reportFollowersByMunicipality.reportFollowersByMunicipality(
            dateStart, dateEnd, typeRol, authIdHeader);

    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    reporte.stream()
        .map(
            reporteTmp -> {
              Optional<UserResponse> userResponse1 =
                  userResponse.stream()
                      .filter(
                          usr -> {
                            return reporteTmp
                                .getMunicipalityId()
                                .equals(Integer.parseInt(usr.getMunicipalityId().toString()));
                          })
                      .findFirst();

              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return reporteTmp
                                .getMunicipalityId()
                                .equals(Integer.parseInt(municipio.getId().toString()));
                          })
                      .findFirst();

              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

              reporteTmp.setNomMun(nombreMunicipio);

              return reporteTmp;
            })
        .collect(Collectors.toList());

    return reporte;
  }

  @Override
  public List<DownloadFollowersDto> downloadFollowersByCordinator(
      String authIdHeader, String typeRol) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    List<DownloadFollowersDto> reporte = null;
    if (typeRol.equals("CORDINATOR_MUNICIPAL")) {
      reporte = downloadFollowersFacade.getAllFollowersByReference(authIdHeader);
    } else if (typeRol.equals("CORDINATOR_ZONE")) {
      reporte = downloadFollowersFacade.getAllFollowers(authIdHeader);
    } else if (typeRol.equals("ADMIN")) {
      reporte = downloadFollowersFacade.getAllByAdmin();
    }

    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorZone();
    List<UserProfileResponse> userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<EntitiesResponse> entitiesResponse = geographyRestClient.readAllEntities();

    reporte.stream()
        .map(
            reporteTmp -> {
              if (reporteTmp.getMunicipioResidenciaId() != null) {
                Optional<MunicipalitiesResponse> muni =
                    municipalitiesResponse.stream()
                        .filter(
                            municipio -> {
                              return reporteTmp
                                  .getMunicipioResidenciaId()
                                  .equals(Integer.parseInt(municipio.getId().toString()));
                            })
                        .findFirst();

                String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");

                reporteTmp.setNombreMunicipio(nombreMunicipio);
              }

              if (reporteTmp.getEntidadResidenciaId() != null) {
                Optional<EntitiesResponse> ent =
                    entitiesResponse.stream()
                        .filter(
                            entidad -> {
                              return reporteTmp
                                  .getEntidadResidenciaId()
                                  .equals(Integer.parseInt(entidad.getId().toString()));
                            })
                        .findFirst();

                String nombreEntidad = ent.map(EntitiesResponse::getNomEnt).orElse("");

                reporteTmp.setNombreEntidad(nombreEntidad);
              }
              if (typeRol.equals("CORDINATOR_MUNICIPAL") || typeRol.equals("ADMIN")) {
                // Obtener los nombres de los coordinadores de zona
                Optional<UserProfileResponse> useProfileCoorOptional =
                    userProfileResponse.stream()
                        .filter(
                            userProfileResponse1 ->
                                reporteTmp
                                    .getCordinatorZoneAuthId()
                                    .equals(userProfileResponse1.getAuthId()))
                        .findFirst();

                String firstNameCoor =
                    useProfileCoorOptional.map(UserProfileResponse::getFirstName).orElse("");
                String lastNameCoor =
                    useProfileCoorOptional.map(UserProfileResponse::getLastName).orElse("");

                reporteTmp.setCordinatorZoneName(firstNameCoor + " " + lastNameCoor);

              } else {
                reporteTmp.setCordinatorZoneName("");
              }

              return reporteTmp;
            })
        .collect(Collectors.toList());

    return reporte;
  }

  @Override
  public ReportFollowersMobilzationDto reporteFollowersMobilizationBySection(Long sectionId) {
    return reportFollowersMobilizationBySectionFacade.reportFollowersMobilization(sectionId);
  }
}
