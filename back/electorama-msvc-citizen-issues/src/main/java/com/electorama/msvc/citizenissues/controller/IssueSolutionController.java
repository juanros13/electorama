package com.electorama.msvc.citizenissues.controller;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionDto;
import com.electorama.msvc.citizenissues.services.IssuesSolutionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/solution")
public class IssueSolutionController {

  @Autowired private IssuesSolutionService issuesSolutionService;

  @GetMapping
  public ResponseEntity getSolutions(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "15") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    return ResponseEntity.ok(
        issuesSolutionService.findAll(pageNo, pageSize, sortBy, orderBy, search));
  }

  @PostMapping
  public ResponseEntity createSolution(@RequestBody IssueSolutionDto issueSolutionDto) {

    return ResponseEntity.ok(issuesSolutionService.save(issueSolutionDto));
  }

  @PutMapping
  public ResponseEntity updateSolution(@RequestBody IssueSolutionDto issueSolutionDto) {

    return ResponseEntity.ok(issuesSolutionService.save(issueSolutionDto));
  }

  @GetMapping("/findSolutionById/{id}")
  public ResponseEntity getSolutionById(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(issuesSolutionService.findById(id));
  }
}
