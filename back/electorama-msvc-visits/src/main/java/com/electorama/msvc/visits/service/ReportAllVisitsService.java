package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.model.dto.DatesRequestDto;
import com.electorama.msvc.visits.model.dto.ReportAllVisitsDto;
import com.electorama.msvc.visits.model.dto.ReportVisitsByTimeDto;
import java.util.List;

public interface ReportAllVisitsService {

  ReportAllVisitsDto findAllVisitsByAdmin();

  List<ReportVisitsByTimeDto> findAllVisitsByTime(DatesRequestDto datesRequestDto);
}
