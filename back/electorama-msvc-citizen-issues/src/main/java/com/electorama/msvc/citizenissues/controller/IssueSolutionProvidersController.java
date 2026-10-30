package com.electorama.msvc.citizenissues.controller;

import com.electorama.msvc.citizenissues.model.entity.IssueSolutionProviderEntity;
import com.electorama.msvc.citizenissues.services.ProvidersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/provider")
public class IssueSolutionProvidersController {

  @Autowired private ProvidersService providersService;

  @GetMapping
  public ResponseEntity getProviders() {

    return ResponseEntity.ok(providersService.findAll());
  }

  @PostMapping
  public ResponseEntity createProviders(@RequestBody IssueSolutionProviderEntity provider) {

    return ResponseEntity.ok(providersService.save(provider));
  }

  @GetMapping("/findProvidersById/{id}")
  public ResponseEntity getProvidersById(
      @RequestHeader("X-Auth-Id") String authIdHeader, @PathVariable("id") Long id) {

    return ResponseEntity.ok(providersService.findById(id));
  }
}
