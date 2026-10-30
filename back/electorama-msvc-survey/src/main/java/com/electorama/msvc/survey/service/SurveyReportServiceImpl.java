package com.electorama.msvc.survey.service;

import com.electorama.msvc.survey.model.dto.*;
import com.electorama.msvc.survey.model.repository.SurveySectionGoalRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SurveyReportServiceImpl implements SurveyReportService {

  @Autowired SurveySectionGoalRepository surveySectionGoalRepository;

  @Override
  public List<MunicipalityDashDto> getDashMunicipality() {
    List<MunicipalityDashDto> result = new ArrayList<>();

    List<Object[]> objects = surveySectionGoalRepository.getDashMunicipality();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      String municipalityName = objArray[1].toString();
      long golHomes = ((Number) objArray[2]).longValue();
      long formCapt = ((Number) objArray[3]).longValue();
      long formToCapt = ((Number) objArray[4]).longValue();

      MunicipalityDashDto dto = new MunicipalityDashDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipalityName(municipalityName);
      dto.setGoalHolmes(golHomes);
      dto.setFormCaptured(formCapt);
      dto.setFormToCapture(formToCapt);
      result.add(dto);
    }
    return result;
  }

  @Override
  public List<MunicipalitySummaryDto> getSurveyReportMunicipality() {
    List<MunicipalitySummaryDto> result = new ArrayList<>();

    List<Object[]> objects = surveySectionGoalRepository.getSurveyReportMunicipality();

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      String municipalityName = objArray[1].toString();
      long section = ((Number) objArray[2]).longValue();
      long golHomes = ((Number) objArray[3]).longValue();
      long folioCaptured = ((Number) objArray[4]).longValue();
      long formCaptured = ((Number) objArray[5]).longValue();

      MunicipalitySummaryDto dto = new MunicipalitySummaryDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipalityName(municipalityName);
      dto.setSection(section);
      dto.setGoalHolmes(golHomes);
      dto.setFolioCaptured(folioCaptured);
      dto.setFormCaptured(formCaptured);
      result.add(dto);
    }
    return result;
  }

  @Override
  public List<CoordinateMunicipalityDto> getCoordinateMunicipality(Long municipalityId) {
    List<CoordinateMunicipalityDto> result = new ArrayList<>();

    List<Object[]> objects = surveySectionGoalRepository.getCoordinatesMunicipality(municipalityId);

    int idCounter = 1;
    for (Object[] objArray : objects) {

      long survetId = ((Number) objArray[0]).longValue();
      String latitude = objArray[1].toString();
      String longitude = objArray[2].toString();

      CoordinateMunicipalityDto dto = new CoordinateMunicipalityDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setSurveyUserId(survetId);
      dto.setLatitude(latitude);
      dto.setLongitude(longitude);
      result.add(dto);
    }
    return result;
  }

  @Override
  public List<SurveyUserGeoDto> getGeolocationByUser(Long userId) {
    List<SurveyUserGeoDto> result = new ArrayList<>();

    List<Object[]> objects = surveySectionGoalRepository.getUserId(userId);

    int idCounter = 1;
    for (Object[] objArray : objects) {

      long survetId = ((Number) objArray[0]).longValue();
      String latitude = objArray[1].toString();
      String longitude = objArray[2].toString();

      SurveyUserGeoDto dto = new SurveyUserGeoDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setSurveyUserId(survetId);
      dto.setLatitude(latitude);
      dto.setLongitude(longitude);
      result.add(dto);
    }
    return result;
  }

  @Override
  public List<MunicipalityGoalDto> getSurveyReportMunicipalityId(Long municipalityId) {
    List<MunicipalityGoalDto> result = new ArrayList<>();

    List<Object[]> objects =
        surveySectionGoalRepository.getSurveyReportMunicipalityId(municipalityId);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long munId = ((Number) objArray[0]).longValue();
      String municipalityName = objArray[1].toString();
      long stateServ = ((Number) objArray[2]).longValue();
      long goalHomes = ((Number) objArray[3]).longValue();
      long folioCaptured = ((Number) objArray[4]).longValue();
      long formCaptured = ((Number) objArray[5]).longValue();
      long homesToBeCensuced = ((Number) objArray[6]).longValue();

      MunicipalityGoalDto dto = new MunicipalityGoalDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(munId);
      dto.setMunicipalityName(municipalityName);
      dto.setStateServer(stateServ);
      dto.setGoalHolmes(goalHomes);
      dto.setFolioCaptured(folioCaptured);
      dto.setFormCaptured(formCaptured);
      dto.setHomesToBeCensuced(homesToBeCensuced);
      result.add(dto);
    }
    return result;
  }

  @Override
  public List<AuthIdMunicipalityDto> getSurveyReportCoordinateMunicipality(
      String authId, Integer sectionNumber) {
    List<AuthIdMunicipalityDto> result = new ArrayList<>();

    List<Object[]> objects =
        surveySectionGoalRepository.getSurveyReportCoordinateMunicipality(authId, sectionNumber);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      String municipalityName = objArray[1].toString();
      long section = ((Number) objArray[2]).longValue();
      long golHomes = ((Number) objArray[3]).longValue();
      long folioCaptured = ((Number) objArray[4]).longValue();
      long formCaptured = ((Number) objArray[5]).longValue();
      String auth = objArray[6].toString();
      String name = objArray[7].toString();

      AuthIdMunicipalityDto dto = new AuthIdMunicipalityDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipalityName(municipalityName);
      dto.setSectionNumber(section);
      dto.setGoalHolmes(golHomes);
      dto.setFolioCaptured(folioCaptured);
      dto.setFormCaptured(formCaptured);
      dto.setAuthId(authId);
      dto.setName(name);
      result.add(dto);
    }
    return result;
  }

  @Override
  public Page<AuthIdMunicipalityDto> getSurveyReportAdminCoordinator(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId) {
    List<AuthIdMunicipalityDto> result = new ArrayList<>();
    Pageable paging =
        PageRequest.of(
            pageNo,
            pageSize,
            orderBy.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending());

    List<Object[]> objects = null;

    if (!search.isEmpty()) {
      log.info("search....");
      objects = surveySectionGoalRepository.getSearchSurveyReportAdminCoordinator(search);
    } else {
      objects = surveySectionGoalRepository.getSurveyReportAdminCoordinator(authId);
    }

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long municipalityId = ((Number) objArray[0]).longValue();
      String municipalityName = objArray[1].toString();
      long section = ((Number) objArray[2]).longValue();
      long golHomes = ((Number) objArray[3]).longValue();
      long folioCaptured = ((Number) objArray[4]).longValue();
      long formCaptured = ((Number) objArray[5]).longValue();
      String auth = objArray[6] != null ? objArray[6].toString() : "";
      String name = objArray[7] != null ? objArray[7].toString() : "";

      AuthIdMunicipalityDto dto = new AuthIdMunicipalityDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setMunicipalityId(municipalityId);
      dto.setMunicipalityName(municipalityName);
      dto.setSectionNumber(section);
      dto.setGoalHolmes(golHomes);
      dto.setFolioCaptured(folioCaptured);
      dto.setFormCaptured(formCaptured);
      dto.setAuthId(auth);
      dto.setName(name);
      result.add(dto);
    }
    // Obtener la sublista correspondiente a la página actual
    int start = (int) paging.getOffset();
    int end = Math.min((start + paging.getPageSize()), result.size());
    List<AuthIdMunicipalityDto> paginatedResult = result.subList(start, end);

    // Crear el objeto Page con los datos paginados
    return new PageImpl<>(paginatedResult, paging, result.size());
  }
}
