package com.electorama.msvc.canvasandwalls.service;

import com.electorama.msvc.canvasandwalls.facade.ReportCanvasAndWallsAllFacade;
import com.electorama.msvc.canvasandwalls.facade.ReportCanvasAndWallsAllResumeFacade;
import com.electorama.msvc.canvasandwalls.model.dto.DatesRequestDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsAllDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsAllResumeDto;
import com.electorama.msvc.canvasandwalls.model.dto.ReportCanvasAndWallsByTimeDto;
import com.electorama.msvc.canvasandwalls.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.canvasandwalls.model.rest.SeccionResponse;
import com.electorama.msvc.canvasandwalls.model.rest.UserProfileResponse;
import com.electorama.msvc.canvasandwalls.service.rest.GeographyRestClient;
import com.electorama.msvc.canvasandwalls.service.rest.UsersRestClient;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportCanvasAndWallsAllServiceImpl implements ReportCanvasAndWallsAllService {

  @Autowired ReportCanvasAndWallsAllFacade reportCanvasAndWallsAllFacade;

  @Autowired UsersRestClient usuariosRestClient;

  @Autowired GeographyRestClient geographyRestClient;
  @Autowired ReportCanvasAndWallsAllResumeFacade reportCanvasAndWallsAllResumeFacade;

  @Override
  public List<ReportCanvasAndWallsAllDto> reportVisitsAll(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(fechasDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(fechasDto.getEndDate()) + "'";
    List<ReportCanvasAndWallsAllDto> reporte =
        reportCanvasAndWallsAllFacade.getReportCanvasAndWallsAll(
            dateStart, dateEnd, typeRol, authIdHeader);
    // List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    List<UserProfileResponse> userProfileResponseMun;

    if (typeRol.equals("CORDINATOR_ZONE_ALT")) {
      userProfileResponseMun = usuariosRestClient.readUserAllCoorMunAlt();
    } else {
      userProfileResponseMun = usuariosRestClient.readUserAllCoorMun();
    }

    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    reporte.stream()
        .map(
            resp -> {
              if (resp.getReferente() != null) {
                Optional<UserProfileResponse> useProfileOptional =
                    userProfileResponseMun.stream()
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
                UserProfileResponse userProfileResponseAdd =
                    usuariosRestClient.readUserXAuthId(resp.getCoordinadorZona());
                if (userProfileResponseAdd != null && userProfileResponseAdd.getId() != null) {
                  String firstName = userProfileResponseAdd.getFirstName();
                  String lastName = userProfileResponseAdd.getLastName();

                  resp.setCoordinadorZona((firstName + " " + lastName).trim());
                }
                //                Optional<UserProfileResponse> useProfileOptional =
                //                    userProfileResponse.stream()
                //                        .filter(
                //                            usrfirstNombre ->
                //
                // resp.getCoordinadorZona().equals(usrfirstNombre.getAuthId()))
                //                        .findFirst();
                //
                //                String firstName =
                //
                // useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
                //                String lastName =
                //
                // useProfileOptional.map(UserProfileResponse::getLastName).orElse("");
                //
                //                resp.setCoordinadorZona(firstName + " " + lastName);
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
  public List<ReportCanvasAndWallsAllResumeDto> reportVisitsAllResume() {
    return reportCanvasAndWallsAllResumeFacade.getReportCanvasAndWallsAllResume();
  }

  @Override
  public List<ReportCanvasAndWallsByTimeDto> findAllVisitsByTime(
      DatesRequestDto datesRequestDto, Integer category) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(datesRequestDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(datesRequestDto.getEndDate()) + "'";

    List<ReportCanvasAndWallsByTimeDto> response =
        reportCanvasAndWallsAllResumeFacade.findAllVisitsByTime(dateStart, dateEnd, category);

    return response;
  }
}
