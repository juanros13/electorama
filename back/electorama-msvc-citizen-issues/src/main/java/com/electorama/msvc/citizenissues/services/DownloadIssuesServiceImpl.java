package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.facade.DownloadIssuesFacade;
import com.electorama.msvc.citizenissues.model.dto.DatesRequestDto;
import com.electorama.msvc.citizenissues.model.dto.DownloadIssuesDto;
import com.electorama.msvc.citizenissues.model.rest.LocalityResponse;
import com.electorama.msvc.citizenissues.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.citizenissues.model.rest.UserProfileResponse;
import com.electorama.msvc.citizenissues.services.rest.GeographyRestClient;
import com.electorama.msvc.citizenissues.services.rest.UsersRestClient;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DownloadIssuesServiceImpl implements DownloadIssuesService {

  @Autowired private DownloadIssuesFacade downloadIssuesFacade;

  @Autowired private UsersRestClient usuariosRestClient;

  @Autowired GeographyRestClient geographyRestClient;

  @Override
  public List<DownloadIssuesDto> downloadIssuesByTime(DatesRequestDto datesRequestDto) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(datesRequestDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(datesRequestDto.getEndDate()) + "'";
    List<DownloadIssuesDto> reporte = downloadIssuesFacade.getAllIssuesByTime(dateStart, dateEnd);
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllCitizenIssues();
    List<MunicipalitiesResponse> municipalitiesResponse =
        geographyRestClient.readMunicipalitiesByEntity();

    List<LocalityResponse> localityResponses = geographyRestClient.readLocalityAll();

    reporte.stream()
        .map(
            resp -> {
              if (resp.getUsuarioCreoId() != null) {
                Optional<UserProfileResponse> useProfileOptional =
                    userProfileResponse.stream()
                        .filter(
                            usrfirstNombre ->
                                resp.getUsuarioCreoId().equals(usrfirstNombre.getAuthId()))
                        .findFirst();

                String firstName =
                    useProfileOptional.map(UserProfileResponse::getFirstName).orElse("");
                String lastName =
                    useProfileOptional.map(UserProfileResponse::getLastName).orElse("");

                resp.setUsuarioCreo(firstName + " " + lastName);
              } else {
                resp.setUsuarioCreo("-SIN USUARIO-");
              }

              if (resp.getMunicipalityId() != null) {
                Optional<MunicipalitiesResponse> muni =
                    municipalitiesResponse.stream()
                        .filter(
                            municipio ->
                                resp.getMunicipalityId()
                                    .toString()
                                    .equals(municipio.getId().toString()))
                        .findFirst();
                String nombreMunicipio = muni.map(MunicipalitiesResponse::getNomMun).orElse("");
                resp.setMunicipio(nombreMunicipio);
              } else {
                resp.setMunicipio("-SIN MUNICIPIO-");
              }

              if (resp.getLocalityId() != null) {
                Optional<LocalityResponse> local =
                    localityResponses.stream()
                        .filter(
                            locali ->
                                resp.getLocalityId().toString().equals(locali.getId().toString()))
                        .findFirst();
                String nombreLocalidad = local.map(LocalityResponse::getLocalidad).orElse("");
                resp.setLocalidad(nombreLocalidad);
              } else {
                resp.setLocalidad("-SIN LOCALIDAD-");
              }
              return resp;
            })
        .collect(Collectors.toList());

    return reporte;
  }
}
