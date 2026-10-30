package com.electorama.msvc.elections.structure.controller;

import com.electorama.msvc.elections.structure.model.dto.RouteDto;
import com.electorama.msvc.elections.structure.service.RouteService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/routes")
public class RouteController {

  @Autowired RouteService routeService;

  @GetMapping("/{idRoute}")
  public ResponseEntity<?> get(
      @PathVariable Long idRoute,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Optional<RouteDto> routeDto = routeService.getById(idRoute);
    // Add the logic to get the polling representative by id
    if (routeDto.isPresent()) {
      return ResponseEntity.ok(routeDto.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/polling-representative-general/{idPollingRepresentativeGeneral}")
  public ResponseEntity<?> getByPollingRepresentativeGeneral(
      @PathVariable Long idPollingRepresentativeGeneral,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Optional<RouteDto> routeDto =
        routeService.getByPollingRepresentativeGeneralId(idPollingRepresentativeGeneral);
    // Add the logic to get the polling representative by id
    if (routeDto.isPresent()) {
      return ResponseEntity.ok(routeDto.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/polling-representative/{idPollingRepresentative}")
  public ResponseEntity<?> getByPollingRepresentative(
      @PathVariable Long idPollingRepresentative,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Optional<RouteDto> routeDto =
        routeService.getByPollingRepresentativeId(idPollingRepresentative);
    // Add the logic to get the polling representative by id
    if (routeDto.isPresent()) {
      return ResponseEntity.ok(routeDto.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
