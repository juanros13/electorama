package com.electorama.elections.controllers;

import com.electorama.elections.models.dto.*;
import com.electorama.elections.services.PollingBoothReportService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/pollingBoothReport")
public class PollingBoothReportController {

  @Autowired PollingBoothReportService pollingBoothReportService;

  @PostMapping("/v2.0/status")
  public ResponseEntity<?> getReportStatusOpen(@RequestBody PollingBoothsIdsDto request) {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "reportStatus",
          pollingBoothReportService.getPollingBoothStatusOpenByPollingBoothIds(
              request.getPollingBoothsIds()));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping("/v2.0/statusResult")
  public ResponseEntity<?> getReportResults(@RequestBody PollingBoothsIdsDto request) {

    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "reportResult",
          pollingBoothReportService.getPollingBoothResultsByPollingBoothIds(
              request.getPollingBoothsIds()));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
