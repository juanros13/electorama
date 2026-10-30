package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.*;
import java.util.List;

public interface ReportFollowersService {

  List<ReportFollowersByCoordinatorZoneDto> findReportFollowersByCoordinatorZone(
      DatesRequestDto fechasDto, String typeRol, Integer municipalityId, String authIdHeader);

  List<ReportFollowersByReferentDto> reportFollowersByReferent(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader);

  List<ReportFollowersByMunicipalityDto> reportFollowersByMunicipality(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader);

  List<DownloadFollowersDto> downloadFollowersByCordinator(String authIdHeader, String typeRol);

  ReportFollowersMobilzationDto reporteFollowersMobilizationBySection(Long sectionId);
}
