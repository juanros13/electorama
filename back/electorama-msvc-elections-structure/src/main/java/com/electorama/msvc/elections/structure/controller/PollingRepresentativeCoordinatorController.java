package com.electorama.msvc.elections.structure.controller;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeCoordinatorDto;
import com.electorama.msvc.elections.structure.service.PollingRepresentativeCoordinatorService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/pollings-representatives-coordinators")
public class PollingRepresentativeCoordinatorController {

  @Autowired PollingRepresentativeCoordinatorService pollingRepresentativeCoordinatorService;

  @GetMapping("/")
  public ResponseEntity<?> getAll(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {

    List<PollingRepresentativeCoordinatorDto> pollingRepresentativeDto =
        pollingRepresentativeCoordinatorService.getAllCoordinators();
    return ResponseEntity.ok(pollingRepresentativeDto);
  }

  @GetMapping("/getByAuthId")
  public ResponseEntity<?> getByAuthId(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {

    Optional<PollingRepresentativeCoordinatorDto> pollingRepresentativeCoordinatorDto =
        pollingRepresentativeCoordinatorService.getByAuthId(authIdHeader);
    if (pollingRepresentativeCoordinatorDto.isPresent()) {
      return ResponseEntity.ok(pollingRepresentativeCoordinatorDto.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
