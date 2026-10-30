package com.electorama.msvc.visits.service;

import com.electorama.msvc.visits.facade.ReportAllVisitsFacade;
import com.electorama.msvc.visits.model.dto.DatesRequestDto;
import com.electorama.msvc.visits.model.dto.ReportAllVisitsDto;
import com.electorama.msvc.visits.model.dto.ReportVisitsByTimeDto;
import java.text.SimpleDateFormat;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportAllVisitsServiceImpl implements ReportAllVisitsService {

  @Autowired ReportAllVisitsFacade reportAllVisitsFacade;

  @Override
  public ReportAllVisitsDto findAllVisitsByAdmin() {
    return reportAllVisitsFacade.getReporteAllVisitsByAdmin();
  }

  @Override
  public List<ReportVisitsByTimeDto> findAllVisitsByTime(DatesRequestDto datesRequestDto) {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    String dateStart = "'" + dateFormat.format(datesRequestDto.getStartDate()) + "'";
    String dateEnd = "'" + dateFormat.format(datesRequestDto.getEndDate()) + "'";

    List<ReportVisitsByTimeDto> response =
        reportAllVisitsFacade.findAllVisitsByTime(dateStart, dateEnd);

    return response;
  }
}
