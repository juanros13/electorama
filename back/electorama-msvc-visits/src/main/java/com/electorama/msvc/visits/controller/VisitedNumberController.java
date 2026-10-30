package com.electorama.msvc.visits.controller;

import com.electorama.msvc.visits.model.dto.VisitedNumberDto;
import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.service.VisitedNumberService;
import com.electorama.msvc.visits.service.VisitedPeopleService;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/visitedNumber")
public class VisitedNumberController {

  @Autowired VisitedNumberService visitedNumberService;

  @Autowired VisitedPeopleService visitedPeopleService;

  @GetMapping("/{visitedPeopleId}")
  public ResponseEntity<?> getByVisitedPaopleId(
      @PathVariable Long visitedPeopleId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    return ResponseEntity.ok(visitedNumberService.byVisitedPeopleId(visitedPeopleId));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteVisitedNumber(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<VisitedNumberDto> o = visitedNumberService.byId(id);
    if (o.isPresent()) {
      VisitedNumberDto visitedDB = o.get();

      Optional<VisitedPeopleDto> visitedPeopleDtoOptional =
          visitedPeopleService.byId(visitedDB.getVisitedPeopleId(), authIdHeader);
      if (visitedPeopleDtoOptional.isPresent()) {
        VisitedPeopleDto visitedPeopleDto = visitedPeopleDtoOptional.get();

        return ResponseEntity.status(HttpStatus.OK)
            .body(visitedNumberService.delete(id, visitedPeopleDto));
      }
    }
    return ResponseEntity.notFound().build();
  }
}
