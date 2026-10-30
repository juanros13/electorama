package com.electorama.msvc.citizenissues.controller;

import com.electorama.msvc.citizenissues.services.*;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class CatalogsController {

  @Autowired private IssuesCategoryService issuesCategoryService;

  @Autowired private IssuesStatusService issuesStatusService;

  @Autowired private IssuesTypesService issuesTypesService;

  @Autowired private IssuesSolutionDocumentsCategoryService IssuesSolutionDocumentsCategoryService;

  @GetMapping("/catIssueCategory")
  public Map<String, Object> catIssueCategory() {

    Map<String, Object> body = new HashMap<>();
    body.put("catIssueCategory", issuesCategoryService.findAll());
    return body;
  }

  @GetMapping("/catIssueStatus")
  public Map<String, Object> catIssueStatus() {

    Map<String, Object> body = new HashMap<>();
    body.put("catIssueStatus", issuesStatusService.findAll());
    return body;
  }

  @GetMapping("/catIssueTypes")
  public Map<String, Object> catIssueTypes() {

    Map<String, Object> body = new HashMap<>();
    body.put("catIssueTypes", issuesTypesService.findAll());
    return body;
  }

  @GetMapping("/catIssueSolutionDocumentsCategory")
  public Map<String, Object> catIssueSolutionDocumentsCategory() {

    Map<String, Object> body = new HashMap<>();
    body.put("catIssueSolutionDocumentsCategory", IssuesSolutionDocumentsCategoryService.findAll());
    return body;
  }
}
