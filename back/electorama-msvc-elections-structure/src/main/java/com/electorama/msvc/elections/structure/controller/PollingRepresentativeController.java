package com.electorama.msvc.elections.structure.controller;

import com.electorama.msvc.elections.structure.model.dto.PollingRepresentativeDto;
import com.electorama.msvc.elections.structure.service.PollingRepresentativeService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/pollings-representatives")
public class PollingRepresentativeController {

  @Autowired PollingRepresentativeService pollingRepresentativeService;

  @GetMapping("/")
  public ResponseEntity<?> getByAuthId(
      @RequestHeader("X-Auth-Id") String authIdHeader, HttpServletRequest servletRequest) {

    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeService.getByAuthId(authIdHeader);
    // Add the logic to get the polling representative by id
    if (pollingRepresentativeDto.isPresent()) {
      return ResponseEntity.ok(pollingRepresentativeDto.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }

  @GetMapping("/polling-booth/{idPollingBooth}")
  public ResponseEntity<?> getByPollingBoth(@PathVariable Long idPollingBooth) {
    log.info("Request received for getByPollingBoth with idPollingBooth: {}", idPollingBooth);

    List<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeService.getByPollingBooth(idPollingBooth);
    log.info("Result from getByPollingBoth: {}", pollingRepresentativeDto);
    return ResponseEntity.ok(pollingRepresentativeDto);
  }

  @GetMapping("/{idPollingRepresentative}")
  public ResponseEntity<?> get(
      @PathVariable Long idPollingRepresentative,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {

    Optional<PollingRepresentativeDto> pollingRepresentativeDto =
        pollingRepresentativeService.get(idPollingRepresentative);
    // Add the logic to get the polling representative by id
    if (pollingRepresentativeDto.isPresent()) {
      return ResponseEntity.ok(pollingRepresentativeDto.get());
    } else {
      return ResponseEntity.notFound().build();
    }
  }
}
