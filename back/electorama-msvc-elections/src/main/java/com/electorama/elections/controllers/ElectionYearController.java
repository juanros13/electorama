package com.electorama.elections.controllers;

import com.electorama.elections.services.ElectionYearService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/electionYear")
public class ElectionYearController {
  @Autowired ElectionYearService electionYearService;

  @GetMapping("/")
  public Map<String, Object> findAllYears(@RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("electionsYears", electionYearService.getAllYears());
    return body;
  }
}
