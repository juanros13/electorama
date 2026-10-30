package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.*;
import java.util.List;

public interface ReportVisitedService {

  List<ReportVisitedByMunicipalCoordinatorDto> findReportVisitedByMunicipalCoordinator(
      DatesRequestDto fechasDto);

  List<ReportVisitsByMunicipalityDto> reportVisitsByMunicipality(DatesRequestDto fechasDto);

  List<ReportVisitMapDto> reportVisitsMap(String typeRol, String authIdHeader);

  List<ReportVisitAllDto> reportVisitsAll(
      DatesRequestDto fechasDto, String typeRol, String authIdHeader);

  List<ReportVisitsByCoordinatorZoneSectionRefDto> reportVisitsByCoordinatorZoneSectionRef(
      DatesRequestDto fechasDto, Integer municipalityId, String typeRol, String authIdHeader);
}
