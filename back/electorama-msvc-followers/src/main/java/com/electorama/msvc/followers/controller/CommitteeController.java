package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.ReportCommitteesGoalsDto;
import com.electorama.msvc.followers.service.CommitteesService;
import jakarta.servlet.http.HttpServletRequest;
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
@RequestMapping(value = "/committees")
public class CommitteeController {

  @Autowired CommitteesService committeesService;

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("ADMIN")) {
        response.put(
            "list",
            committeesService.listAdmin(pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
        response.put("success", "true");
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
      } else {
        response.put(
            "list",
            committeesService.list(pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
        response.put("success", "true");
        return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
      }
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<>(
          "No se encontraron datos con datos asociados el id: " + authIdHeader,
          HttpStatus.NOT_FOUND);
    }
  }

  @GetMapping("/reportCommitteesGoals")
  public ResponseEntity create(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {
    ReportCommitteesGoalsDto reportCommitteesGoalsDto;
    if (servletRequest.isUserInRole("ADMIN")) {
      reportCommitteesGoalsDto = committeesService.getReportCommitteesGoalsByAdmin();
    } else {
      reportCommitteesGoalsDto = committeesService.getReportCommitteesGoals(authIdHeader);
    }
    return ResponseEntity.ok(reportCommitteesGoalsDto);
  }
}
