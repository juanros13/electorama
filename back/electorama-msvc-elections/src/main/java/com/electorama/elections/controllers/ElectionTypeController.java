package com.electorama.elections.controllers;

import com.electorama.elections.services.ElectionTypeService;
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
@RequestMapping(value = "/electionType")
public class ElectionTypeController {

  @Autowired ElectionTypeService electionTypeService;

  @GetMapping("/")
  public Map<String, Object> findAllYears(@RequestHeader("X-Auth-Id") String authIdHeader) {

    Map<String, Object> body = new HashMap<>();
    body.put("electionsTypes", electionTypeService.getAllTypes());
    return body;
  }
}
