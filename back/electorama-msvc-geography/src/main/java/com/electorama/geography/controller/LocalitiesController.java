package com.electorama.geography.controller;

import com.electorama.geography.service.LocalitiesService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/v1.0.0/Localities")
@RequiredArgsConstructor
public class LocalitiesController {

  @Autowired LocalitiesService localitiesService;

  @GetMapping(value = "")
  public ResponseEntity readLocalities(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "10") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest request) {

    if (request.isUserInRole("ADMIN")) {
      log.info("Es Administrador");
      return ResponseEntity.ok(
          localitiesService.findByLocalities(pageNo, pageSize, sortBy, orderBy, search));
    } else {
      log.info("otro");
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping(value = "/localitiesByMunicipality/{id}")
  public ResponseEntity readLocalitiesByMunicipality(
      HttpServletRequest request, @PathVariable("id") String id) {

    return ResponseEntity.ok(localitiesService.findByLocalitiesByMunicipality(id));
  }
}
