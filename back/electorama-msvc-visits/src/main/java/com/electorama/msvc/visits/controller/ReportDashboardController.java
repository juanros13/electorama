package com.electorama.msvc.visits.controller;

import com.electorama.msvc.visits.model.dto.DatesRequestDto;
import com.electorama.msvc.visits.model.dto.ReportAllVisitsDto;
import com.electorama.msvc.visits.model.dto.ReportVisitsByTimeDto;
import com.electorama.msvc.visits.service.ReportAllVisitsService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/reportVisitedDashboard")
public class ReportDashboardController {

  @Autowired ReportAllVisitsService reportAllVisitsService;

  @GetMapping("/reportTotalVisit")
  public ResponseEntity<?> reportTotalVisits(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    ReportAllVisitsDto reportAllVisitsDtos = null;
    if (servletRequest.isUserInRole("ADMIN")) {
      reportAllVisitsDtos = reportAllVisitsService.findAllVisitsByAdmin();
    }
    if (reportAllVisitsDtos != null) {
      return ResponseEntity.ok(reportAllVisitsDtos);
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping("/reportTotalVisitByTime")
  public ResponseEntity<?> reportAllVisitsByTime(
      @RequestBody DatesRequestDto datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    List<ReportVisitsByTimeDto> reportAllVisitsDtos = null;
    if (servletRequest.isUserInRole("ADMIN")) {
      reportAllVisitsDtos = reportAllVisitsService.findAllVisitsByTime(datesRequestDto);
    }
    if (reportAllVisitsDtos != null) {
      return ResponseEntity.ok(reportAllVisitsDtos);
    }
    return ResponseEntity.notFound().build();
  }
}
