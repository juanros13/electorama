package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.PollingBoothsIdsDto;
import com.electorama.msvc.followers.model.dto.ReportFollowersMarkByPollingBoothDto;
import com.electorama.msvc.followers.model.dto.UpdateVoteFlagAndSyncronizedListDto;
import com.electorama.msvc.followers.service.FollowersMarkService;
import com.electorama.msvc.followers.service.ScheduleService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.json.internal.json_simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/followersMark")
public class FollowersMarkController {

  @Autowired private ScheduleService scheduleService;
  @Autowired private FollowersMarkService followersMarkService;

  @GetMapping("/v2.0/getFollowerMark")
  public ResponseEntity<?> getFollowerMark(
      @RequestBody PollingBoothsIdsDto pollingBoothsIdsDto,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "getFollowerMark",
          followersMarkService.getFollowerMark(pollingBoothsIdsDto.getPollingBoothsIds()));
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

  @PostMapping("/v2.0/updateVoteFlagAndSyncronizedList")
  public ResponseEntity<?> updateVoteFlagAndSyncronizedList(
      @RequestBody UpdateVoteFlagAndSyncronizedListDto request,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    JSONObject jsonResponse = new JSONObject();
    if (scheduleService.isExecutionAllowed()) {
      // Ejecutar la lógica del negocio
      boolean updated = followersMarkService.updateVoteFlagAndSyncronizedList(request);
      try {
        if (updated) {
          jsonResponse.put("message", "Results updated successfully");
        }
      } catch (Exception e) {
        JSONObject errorResponse = new JSONObject();
        errorResponse.put("error", "Failed to update results");
        errorResponse.put("details", e.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
      }
      return ResponseEntity.ok(jsonResponse);
    } else {
      // No ejecutar la lógica del negocio
      jsonResponse.put("message", "Tarea no permitida en este horario.");
      return ResponseEntity.status(HttpStatus.FORBIDDEN).body(jsonResponse);
    }
  }

  @PostMapping("/v2.0/getReportMarkByPollingBooths")
  public ResponseEntity<?> reportMarkByPollingBooths(
      @RequestBody PollingBoothsIdsDto request, @RequestHeader("X-Auth-Id") String authIdHeader) {

    JSONObject jsonResponse = new JSONObject();
    try {
      List<ReportFollowersMarkByPollingBoothDto> reportFollowersMarkByPollingBoothDtoList =
          followersMarkService.getReportFollowersMobilzationByPollingBooth(request);
      if (!reportFollowersMarkByPollingBoothDtoList.isEmpty()) {
        jsonResponse.put("success", "true");
        jsonResponse.put("reportMark", reportFollowersMarkByPollingBoothDtoList);
        return new ResponseEntity<Map<String, Object>>(jsonResponse, HttpStatus.OK);
      }
    } catch (Exception e) {
      JSONObject errorResponse = new JSONObject();
      errorResponse.put("error", "Failed to update results");
      errorResponse.put("details", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    return ResponseEntity.ok(jsonResponse);
  }

  @PostMapping("/getFollowerMarkProgress")
  public ResponseEntity getFollowerMarkProgress(
      @RequestBody PollingBoothsIdsDto pollingBoothsIdsDto,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      return ResponseEntity.ok(
          followersMarkService.getProgressByFollowerMark(
              pollingBoothsIdsDto.getPollingBoothsIds()));
    } catch (DataAccessException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @PostMapping("/getProgressVote")
  public ResponseEntity getProgressVote(
      @RequestBody PollingBoothsIdsDto pollingBoothsIdsDto,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      return ResponseEntity.ok(
          followersMarkService.getProgressVote(pollingBoothsIdsDto.getPollingBoothsIds()));
    } catch (DataAccessException e) {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/getFollowerMarkVoters/{id}")
  public ResponseEntity<?> getFollowerMarkVoters(
      @PathVariable Integer id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put("getFollowerMarkVoters", followersMarkService.getVotersParams(id));
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
