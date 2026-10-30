package com.electorama.msvc.electionsresults.controllers;

import com.electorama.msvc.electionsresults.models.dto.ElectionAssociationIdsDto;
import com.electorama.msvc.electionsresults.models.dto.ResultDto;
import com.electorama.msvc.electionsresults.models.dto.UpdateElectionsResultsSheetsDto;
import com.electorama.msvc.electionsresults.service.ElectionsResultsSheetsService;
import com.electorama.msvc.electionsresults.service.ScheduleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/electionsResultsSheets")
public class ElectionsResultsSheetsController {

  @Autowired ElectionsResultsSheetsService electionsResultsSheetsService;
  @Autowired private ScheduleService scheduleService;

  @PostMapping("/updateElectionsResultsSheets")
  public ResponseEntity<?> updateElectionsResultsSheets(
      @RequestBody List<UpdateElectionsResultsSheetsDto> datesRequestDto,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    JSONObject jsonResponse = new JSONObject();

    try {
      electionsResultsSheetsService.updateElectionsResultsSheets(
          (List<UpdateElectionsResultsSheetsDto>) datesRequestDto);

      jsonResponse.put("message", "Results updated successfully");
      return ResponseEntity.ok(jsonResponse);
    } catch (Exception e) {
      JSONObject errorResponse = new JSONObject();
      errorResponse.put("error", "Failed to update results");
      errorResponse.put("details", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
  }

  @PostMapping("/findElectionsAssociation")
  public List<ResultDto> findElectionsAssociation(
      @RequestBody ElectionAssociationIdsDto electionAssociationIds) {
    return electionsResultsSheetsService.findElectionsAssociation(electionAssociationIds);
  }
}
