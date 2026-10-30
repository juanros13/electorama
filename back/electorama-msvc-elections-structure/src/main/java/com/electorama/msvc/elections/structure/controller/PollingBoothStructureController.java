package com.electorama.msvc.elections.structure.controller;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeAssociationDto;
import com.electorama.msvc.elections.structure.service.PollingRepresentativeService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/pollingsbooth-structure")
public class PollingBoothStructureController {

  @Autowired PollingRepresentativeService pollingRepresentativeService;

  @GetMapping("/v2.0/")
  public ResponseEntity<?> getByAuthId(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {

    if (servletRequest.isUserInRole("RC")) {
      PollingRepresentativeAssociationDto pollingRepresentativeAssociationDto =
          pollingRepresentativeService.getPollingRepresentativeAssociation(authIdHeader);
      if (pollingRepresentativeAssociationDto != null) {
        return ResponseEntity.ok(pollingRepresentativeAssociationDto);
      }
    } else if (servletRequest.isUserInRole("RG")) {
      PollingRepresentativeAssociationDto pollingRepresentativeAssociationDto =
          pollingRepresentativeService.getPollingRepresentativeAssociationByRg(authIdHeader);
      if (pollingRepresentativeAssociationDto != null) {
        return ResponseEntity.ok(pollingRepresentativeAssociationDto);
      }
    }
    return ResponseEntity.badRequest().build();
  }
}
