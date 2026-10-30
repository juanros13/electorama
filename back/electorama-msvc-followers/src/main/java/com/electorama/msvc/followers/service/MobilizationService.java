package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

public interface MobilizationService {
  StateMobilizationDto getStateMobilization();

  List<MunicipalityMobilizationDto> getMunicipalityMobilization();

  List<MunicipalityByReferentMobilizationDto> getMunicipalityByReferentMobilization(
      String authIdHeader);

  List<ResultFederalDistrictDto> getFederalDistrictByReferentMobilization();

  List<SectionByMunicipalityMobilizationDto> getMobilizationSectionByMunicipality();

  List<MunicipalityByReferentAndCoordinatorMobilizationDto>
      getMunicipalityByReferentAndCoordinatorMobilization(
          HttpServletRequest servletRequest,
          String authIdHeader,
          Integer municipalityId,
          String referentId,
          Integer federalDistrict,
          Integer localDistrict);

  List<MunicipalityBySectionAndReferentMobilizationDto>
      getMunicipalityBySectionAndReferentMobilization(Long municipalityId);

  boolean updateVoteFlagAndSyncronizedList(
      UpdateVoteFlagAndSyncronizedListDto updateVoteFlagAndSyncronizedListDto);

  List<FederalDistrictDto> getFederalDistrictMobilization();

  List<LocalDistrictDto> getLocalDistrictMobilization();

  List<ResultFederalDistrictDto> getReferentsByFederalDistrictMobilization(Long federalDistrictId);

  List<ResultLocalDistrictDto> getReferentsByLocalDistrictMobilization(Long localDistrictId);

  List<MunicipalityByReferentAndCoordinatorMobilizationDto> getReferentAlt(
      HttpServletRequest servletRequest, String authIdHeader);

  List<MobilizationAltDto> getMobilizationAlt(
      HttpServletRequest servletRequest, String authIdHeader);

  List<MunicipalityByReferentAndCoordinatorMobilizationDto>
      getMunicipalityByReferentAltAndCoordinatorMobilization(
          HttpServletRequest servletRequest, String authIdHeader, String referentAltId);
}
