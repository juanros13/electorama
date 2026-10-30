package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.facade.*;
import com.electorama.msvc.visits.model.dto.*;
import com.electorama.msvc.visits.model.rest.*;
import com.electorama.msvc.visits.service.rest.GeographyRestClient;
import com.electorama.msvc.visits.service.rest.UsersRestClient;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ReportVisitedServiceImpl implements ReportVisitedService {

  @Autowired ReportVisitedByMunicipalCoordinatorFacade reportVisitedByMunicipalCoordinatorFacade;

  @Autowired ReportVisitsByMunicipalityFacade reportVisitsByMunicipalityFacade;

  @Autowired ReportAllVisitsFacade reportAllVisitsFacade;

  @Autowired
  ReportVisitsByCoordinatorZoneSectionRefFacade reportVisitsByCoordinatorZoneSectionRefFacade;

  @Autowired UsersRestClient usuariosRestClient;

  @Autowired GeographyRestClient geographyRestClient;

  @Override
  public List<ReportVisitedByMunicipalCoordinatorDto> findReportVisitedByMunicipalCoordinator(
      DatesRequestDto fechasDto) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";

    List<UserResponse> userResponse = usuariosRestClient.readUsers();

    List<ReportVisitedByMunicipalCoordinatorDto> reporte =
        reportVisitedByMunicipalCoordinatorFacade.findReportVisitedByMunicipalCoordinator(
            dateStart, dateEnd);

    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCoorMun();

    List<UserMunicipalityResponse> userWithIdMun = usuariosRestClient.readUserAllCoorMunWithIdMun();

    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    List<ReportVisitedByMunicipalCoordinatorDto> responseReporte = new ArrayList<>();
    AtomicReference<Long> count = new AtomicReference<>(1L);
    userWithIdMun.stream()
        .map(
            coordMunWithIdMun -> {
              ReportVisitedByMunicipalCoordinatorDto newRep =
                  new ReportVisitedByMunicipalCoordinatorDto();
              newRep.setReferenceAuthId(coordMunWithIdMun.getUserAuthId());
              newRep.setMunicipalityId(Integer.parseInt(coordMunWithIdMun.getMunicipalityId()));
              newRep.setId(count.get());
              Optional<MunicipalitiesResponse> muni =
                  municipalitiesResponse.stream()
                      .filter(
                          municipio -> {
                            return coordMunWithIdMun
                                .getMunicipalityId()
                                .equals(municipio.getId().toString());
                          })
                      .findFirst();
              String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
              newRep.setMeta(
                  userResponse.stream()
                      .filter(
                          u -> {
                            if (u.getReferenceAuthId().equals(coordMunWithIdMun.getUserAuthId())
                                && u.getMunicipalityId()
                                    .toString()
                                    .equals(coordMunWithIdMun.getMunicipalityId())) {
                              return true;
                            } else {
                              return false;
                            }
                          })
                      .map(UserResponse::getMeta)
                      .findFirst()
                      .orElse(0));
              newRep.setNomMun(nombreMunicipio);

              Optional<UserProfileResponse> useProfileOptional2 =
                  userProfileResponse.stream()
                      .filter(
                          usrfirstNombre ->
                              coordMunWithIdMun.getUserAuthId().equals(usrfirstNombre.getAuthId()))
                      .findFirst();
              String firstName =
                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse("");

              newRep.setFirstName(firstName);

              String lastName =
                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse("");

              newRep.setLastName(lastName);

              Optional<ReportVisitedByMunicipalCoordinatorDto> reportOptinal =
                  reporte.stream()
                      .filter(
                          u -> {
                            if (u.getReferenceAuthId().equals(coordMunWithIdMun.getUserAuthId())
                                && u.getMunicipalityId()
                                    .toString()
                                    .equals(coordMunWithIdMun.getMunicipalityId())) {
                              return true;
                            } else {
                              return false;
                            }
                          })
                      .findFirst();
              Integer primerVisita =
                  reportOptinal
                      .map(ReportVisitedByMunicipalCoordinatorDto::getPrimeraVisita)
                      .orElse(0);
              Integer segundaVisita =
                  reportOptinal
                      .map(ReportVisitedByMunicipalCoordinatorDto::getSegundaVisita)
                      .orElse(0);
              Integer terceraVisita =
                  reportOptinal
                      .map(ReportVisitedByMunicipalCoordinatorDto::getTerceraVisita)
                      .orElse(0);

              Integer abrieronPuerta =
                  reportOptinal
                      .map(ReportVisitedByMunicipalCoordinatorDto::getVisitasAbrieronLaPuerta)
                      .orElse(0);

              Integer noAbrieronPuerta =
                  reportOptinal
                      .map(ReportVisitedByMunicipalCoordinatorDto::getVisitasNoAbrieronLaPuerta)
                      .orElse(0);

              newRep.setPrimeraVisita(primerVisita);
              newRep.setSegundaVisita(segundaVisita);
              newRep.setTerceraVisita(terceraVisita);
              newRep.setVisitasNoAbrieronLaPuerta(noAbrieronPuerta);
              newRep.setVisitasAbrieronLaPuerta(abrieronPuerta);

              responseReporte.add(newRep);
              count.getAndSet(count.get() + 1);
              return coordMunWithIdMun;
            })
        .collect(Collectors.toList());

    //      AtomicReference<Long> count = new AtomicReference<>(1L);
    //      userProfileResponse.stream()
    //        .map(coordMun -> {
    //            ReportVisitedByMunicipalCoordinatorDto newRep = new
    // ReportVisitedByMunicipalCoordinatorDto();
    //            newRep.setMeta(coordMun.getMeta());
    //            newRep.setId(count.get());
    //            newRep.setReferenceAuthId(coordMun.getAuthId());
    //            newRep.setMeta(
    //                  userResponse.stream()
    //                      .filter(u -> u.getReferenceAuthId().equals(coordMun.getAuthId()))
    //                      .map(UserResponse::getMeta)
    //                      .findFirst().orElse(0)
    //                      );
    //            newRep.setFirstName(coordMun.getFirstName());
    //            newRep.setLastName(coordMun.getLastName());
    //
    //            Optional<ReportVisitedByMunicipalCoordinatorDto> reportOptional =
    //                    reporte.stream()
    //                      .filter(
    //                          rep ->
    //                                  rep.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
    //                      .findFirst();
    //            String reportUser = muni.map(MunicipalitiesResponse::getNomMun).orElse(null);
    //
    //            Optional<MunicipalitiesResponse> muni =
    //                  municipalitiesResponse.stream()
    //                      .filter(
    //                          municipio -> {
    //                            return reporteTmp
    //                                .getMunicipalityId()
    //                                .equals(Integer.parseInt(municipio.getId().toString()));
    //                          })
    //                      .findFirst();
    //              String nombreMunicipio =
    // muni.map(MunicipalitiesResponse::getNomMun).orElse(null);
    //
    //              reporteTmp.setNomMun(nombreMunicipio);
    //
    //            count.getAndSet(count.get() + 1);
    //            responseReporte.add(newRep);
    //            return coordMun;
    //        }).collect(Collectors.toList());

    //
    //    reporte.stream()
    //        .map(
    //            reporteTmp -> {
    //              reporteTmp.setMeta(
    //                  userResponse.stream()
    //                      .filter(u ->
    // u.getReferenceAuthId().equals(reporteTmp.getReferenceAuthId()))
    //                      .map(UserResponse::getMeta)
    //                      .findFirst()
    //                      .get());
    //              Optional<UserProfileResponse> useProfileOptional2 =
    //                  userProfileResponse.stream()
    //                      .filter(
    //                          usrfirstNombre ->
    //
    // reporteTmp.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
    //                      .findFirst();
    //              String firstName =
    //                  useProfileOptional2.map(UserProfileResponse::getFirstName).orElse(null);
    //
    //              reporteTmp.setFirstName(firstName);
    //
    //              String lastName =
    //                  useProfileOptional2.map(UserProfileResponse::getLastName).orElse(null);
    //
    //              reporteTmp.setLastName(lastName);
    //
    //              Optional<MunicipalitiesResponse> muni =
    //                  municipalitiesResponse.stream()
    //                      .filter(
    //                          municipio -> {
    //                            return reporteTmp
    //                                .getMunicipalityId()
    //                                .equals(Integer.parseInt(municipio.getId().toString()));
    //                          })
    //                      .findFirst();
    //              String nombreMunicipio =
    // muni.map(MunicipalitiesResponse::getNomMun).orElse(null);
    //
    //              reporteTmp.setNomMun(nombreMunicipio);
    //
    //              return reporteTmp;
    //            })
    //        .collect(Collectors.toList());

    log.info("para");

    return responseReporte;
  }

  @Override
  public List<ReportVisitsByMunicipalityDto> reportVisitsByMunicipality(DatesRequestDto fechasDto) {

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";

    List<UserResponse> userResponse = usuariosRestClient.readMetaMunicipalities();

    List<ReportVisitsByMunicipalityDto> reporte =
        reportVisitsByMunicipalityFacade.findReportVisitsByMunicipality(dateStart, dateEnd);

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

              Integer meta = userResponse1.map(UserResponse::getMeta).orElse(0);

              reporteTmp.setMeta(meta);

              Integer numCoordinadores =
                  userResponse1.map(UserResponse::getCoordinadores).orElse(0);

              reporteTmp.setCoordinadores(numCoordinadores);

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
  public List<ReportVisitMapDto> reportVisitsMap(String typeRol, String authIdHeader) {
    List<ReportVisitMapDto> response = reportAllVisitsFacade.reportVisitsMap(typeRol, authIdHeader);

    return response;
  }

  @Override
  public List<ReportVisitAllDto> reportVisitsAll(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";
    List<ReportVisitAllDto> reporte =
        reportAllVisitsFacade.reportVisitsAll(dateStart, dateEnd, typeRol, authIdHeader);
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    reporte.stream()
        .map(
            resp -> {
              if (resp.getReferente() != null) {
                Optional<UserProfileResponse> useProfileOptional =
                    userProfileResponse.stream()
                        .filter(
                            usrfirstNombre ->
                                resp.getReferente().equals(usrfirstNombre.getAuthId()))
                        .findFirst();

                String firstName =
                    useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
                String lastName =
                    useProfileOptional.map(UserProfileResponse::getLastName).orElse("");

                resp.setReferente(firstName + " " + lastName);
              } else {
                resp.setReferente("-SIN REFERENTE-");
              }
              if (resp.getCoordinadorZona() != null) {
                Optional<UserProfileResponse> useProfileOptional =
                    userProfileResponse.stream()
                        .filter(
                            usrfirstNombre ->
                                resp.getCoordinadorZona().equals(usrfirstNombre.getAuthId()))
                        .findFirst();

                String firstName =
                    useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
                String lastName =
                    useProfileOptional.map(UserProfileResponse::getLastName).orElse("");

                resp.setCoordinadorZona(firstName + " " + lastName);
              } else {
                resp.setCoordinadorZona("-SIN COORDINADOR-");
              }
              if (resp.getMunicipio() != null) {
                Optional<MunicipalitiesResponse> muni =
                    municipalitiesResponse.stream()
                        .filter(
                            municipio -> {
                              return resp.getMunicipio().equals(municipio.getId().toString());
                            })
                        .findFirst();
                String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
                resp.setMunicipio(nombreMunicipio);
              } else {
                resp.setMunicipio("-SIN MUNICIPIO-");
              }

              if (resp.getSeccion() != null) {
                Optional<SeccionResponse> secc =
                    seccionResponse.stream()
                        .filter(
                            seccion -> {
                              return resp.getSeccion().equals(seccion.getId().toString());
                            })
                        .findFirst();

                Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
                resp.setSeccion(seccionNumber != null ? seccionNumber.toString() : "");
              } else {
                resp.setSeccion("-SIN SECCION-");
              }
              return resp;
            })
        .collect(Collectors.toList());

    return reporte;
  }

  @Override
  public List<ReportVisitsByCoordinatorZoneSectionRefDto> reportVisitsByCoordinatorZoneSectionRef(
      DatesRequestDto fechasDto, Integer municipalityId, String typeRol, String authIdHeader) {

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

    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();

    List<ReportVisitsByCoordinatorZoneSectionRefDto> reporte =
        reportVisitsByCoordinatorZoneSectionRefFacade.findReportVisitsByCoordinatorZoneSectionRef(
            dateStart, dateEnd, municipalityId, typeRol, authIdHeader);

    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();
    List<ReportVisitsByCoordinatorZoneSectionRefDto> response = new ArrayList<>();
    for (UserCoordinateResponse userMeta : userCoordinateResponse) {
      ReportVisitsByCoordinatorZoneSectionRefDto reponseReport =
          new ReportVisitsByCoordinatorZoneSectionRefDto();
      reporte.stream()
          .map(
              rep -> {
                if (userMeta.getCoordinateZoneId().equals(rep.getCoordinateZoneId())
                    && userMeta.getSeccionId().equals(rep.getSeccionId())) {
                  reponseReport.setId(rep.getId());
                  reponseReport.setMunicipalityId(rep.getMunicipalityId());
                  reponseReport.setReferenceAuthId(rep.getReferenceAuthId());
                  reponseReport.setNombreCoordinateZone(rep.getNombreCoordinateZone());
                  reponseReport.setSeccionId(rep.getSeccionId());
                  reponseReport.setSeccionNumber(rep.getSeccionNumber());
                  reponseReport.setCoordinateZoneId(rep.getCoordinateZoneId());
                  reponseReport.setNombreCoordinateZone(rep.getNombreCoordinateZone());
                  reponseReport.setPrimeraVisita(rep.getPrimeraVisita());
                  reponseReport.setSegundaVisita(rep.getSegundaVisita());
                  reponseReport.setTerceraVisita(rep.getTerceraVisita());
                  reponseReport.setNomMun(rep.getNomMun());
                  reponseReport.setVisitasNoAbrieronLaPuerta(rep.getVisitasNoAbrieronLaPuerta());
                  reponseReport.setVisitasAbrieronLaPuerta(rep.getVisitasAbrieronLaPuerta());
                }
                return rep;
              })
          .collect(Collectors.toList());

      // ReportVisitsByCoordinatorZoneSectionRefDto reponseReport = report.get();

      reponseReport.setCoordinateZoneId(userMeta.getCoordinateZoneId());
      reponseReport.setReferenceAuthId(userMeta.getReferenceAuthId());
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
      if (reponseReport.getReferenceAuthId() != null) {
        Optional<UserProfileResponse> useProfileOptional =
            userProfileResponse.stream()
                .filter(
                    usrfirstNombre ->
                        reponseReport.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
                .findFirst();

        String firstName = useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
        String lastName = useProfileOptional.map(UserProfileResponse::getLastName).orElse("");

        reponseReport.setNombreReferenceAuth(firstName + " " + lastName);
      }
      Optional<UserProfileResponse> useProfileCoorOptional =
          userProfileResponse.stream()
              .filter(
                  userProfileResponse1 ->
                      reponseReport.getCoordinateZoneId().equals(userProfileResponse1.getAuthId()))
              .findFirst();

      String firstNameCoor =
          useProfileCoorOptional.map(UserProfileResponse::getFirstName).orElse("");
      String lastNameCoor = useProfileCoorOptional.map(UserProfileResponse::getLastName).orElse("");

      reponseReport.setNombreCoordinateZone(firstNameCoor + " " + lastNameCoor);

      if (reponseReport.getPrimeraVisita() == null) {
        reponseReport.setPrimeraVisita(0);
      }
      reponseReport.setMeta(userMeta.getMeta());
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
    //    reporte.stream()
    //        .map(
    //            reporteTmp -> {
    //              Optional<UserCoordinateResponse> coordinateResponse =
    //                  userCoordinateResponse.stream()
    //                      .filter(
    //                          usr -> {
    //                            if (reporteTmp
    //                                    .getMunicipalityId()
    //
    // .equals(Integer.parseInt(usr.getMunicipalityId().toString()))
    //                                && reporteTmp.getSeccionId().equals(usr.getSeccionId())
    //                                && reporteTmp
    //                                    .getCoordinateZoneId()
    //                                    .equals(usr.getCoordinateZoneId())) {
    //                              return true;
    //                            } else {
    //                              return false;
    //                            }
    //                          })
    //                      .findFirst();
    //
    //              Integer meta =
    // coordinateResponse.map(UserCoordinateResponse::getMeta).orElse(null);
    //
    //              reporteTmp.setMeta(meta);
    //
    //              Optional<MunicipalitiesResponse> muni =
    //                  municipalitiesResponse.stream()
    //                      .filter(
    //                          municipio -> {
    //                            return reporteTmp
    //                                .getMunicipalityId()
    //                                .equals(Integer.parseInt(municipio.getId().toString()));
    //                          })
    //                      .findFirst();
    //
    //              String nombreMunicipio =
    // muni.map(MunicipalitiesResponse::getNomMun).orElse(null);
    //
    //              reporteTmp.setNomMun(nombreMunicipio);
    //
    //              Optional<SeccionResponse> secc =
    //                  seccionResponse.stream()
    //                      .filter(
    //                          seccion -> {
    //                            return reporteTmp
    //                                .getSeccionId()
    //                                .equals(Integer.parseInt(seccion.getId().toString()));
    //                          })
    //                      .findFirst();
    //
    //              Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
    //
    //              reporteTmp.setSeccionNumber(seccionNumber);
    //
    //              Optional<UserProfileResponse> useProfileOptional =
    //                  userProfileResponse.stream()
    //                      .filter(
    //                          usrfirstNombre ->
    //
    // reporteTmp.getReferenceAuthId().equals(usrfirstNombre.getAuthId()))
    //                      .findFirst();
    //
    //              String firstName =
    //                  useProfileOptional.map(UserProfileResponse::getFirstName).orElse(null);
    //              String lastName =
    //                  useProfileOptional.map(UserProfileResponse::getLastName).orElse(null);
    //
    //              reporteTmp.setNombreReferenceAuth(firstName + " " + lastName);
    //
    //              useProfileOptional =
    //                  userProfileResponse.stream()
    //                      .filter(
    //                          userProfileResponse1 ->
    //                              reporteTmp
    //                                  .getCoordinateZoneId()
    //                                  .equals(userProfileResponse1.getAuthId()))
    //                      .findFirst();
    //
    //              firstName =
    // useProfileOptional.map(UserProfileResponse::getFirstName).orElse(null);
    //              lastName =
    // useProfileOptional.map(UserProfileResponse::getLastName).orElse(null);
    //
    //              reporteTmp.setNombreCoordinateZone(firstName + " " + lastName);
    //
    //              return reporteTmp;
    //            })
    //        .collect(Collectors.toList());

    return response;
  }
}
