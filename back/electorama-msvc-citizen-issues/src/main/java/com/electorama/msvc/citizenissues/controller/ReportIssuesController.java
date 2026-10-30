package com.electorama.msvc.citizenissues.controller;

import com.electorama.msvc.citizenissues.model.dto.RequestReportDto;
import com.electorama.msvc.citizenissues.services.ReportIssuesService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/reports")
public class ReportIssuesController {

  @Autowired ReportIssuesService reportIssuesService;

  @PostMapping("/getIssuesStatus")
  public ResponseEntity updateIssue(
      @RequestHeader("X-Auth-Id") String authIdHeader,
      @RequestBody RequestReportDto requestReportDto) {
    return ResponseEntity.ok(
        reportIssuesService.getReportIssuesStatusDate(
            requestReportDto.getMunicipalityId(),
            requestReportDto.getStartDate(),
            requestReportDto.getEndDate()));
  }
}
