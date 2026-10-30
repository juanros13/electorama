package com.electorama.msvc.visits.controller;

import com.electorama.msvc.visits.model.dto.VisitedDto;
import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.service.VisitedPeopleService;
import com.electorama.msvc.visits.service.VisitedService;
import jakarta.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/visited")
public class VisitedController {

  @Autowired private VisitedService visitedService;

  @Autowired VisitedPeopleService visitedPeopleService;

  @GetMapping()
  public ResponseEntity<?> list(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put("visited", visitedService.list(pageNo, pageSize, sortBy, orderBy, search));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PostMapping
  public ResponseEntity create(
      @Valid @RequestBody VisitedDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating follower with {}", request.toString());
    VisitedDto visited = visitedService.save(request, authIdHeader);
    return ResponseEntity.ok(visited);
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> edit(
      @Valid @RequestBody VisitedDto visited,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    Optional<VisitedDto> visitDto = visitedService.byId(id, authIdHeader);
    if (visitDto.isPresent()) {
      VisitedDto visitedDB = visitDto.get();

      visitedDB.setMunicipalityId(visited.getMunicipalityId());
      visitedDB.setSeccionId(visited.getSeccionId());
      visitedDB.setCoordinateZoneId(visited.getCoordinateZoneId());
      visitedDB.setLocalityId(visited.getLocalityId());
      visitedDB.setVisitDate(visited.getVisitDate());
      visitedDB.setNumberOfVisited(visitedDB.getVisitedPeople().size());
      visitedDB.setUserCreatedId(visited.getUserCreatedId());

      visitedDB.setVisitedPeople(visited.getVisitedPeople());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(visitedService.update(visitedDB, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteVisited(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<VisitedDto> o = visitedService.byId(id, authIdHeader);
    if (o.isPresent()) {
      VisitedDto PredioDB = o.get();
      long miliseconds = System.currentTimeMillis();
      Date date = new Date(miliseconds);

      PredioDB.setDeleted(date);
      return ResponseEntity.status(HttpStatus.OK).body(visitedService.save(PredioDB, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/visitedPeople/{id}")
  public ResponseEntity<?> deleteVisitedPeople(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<VisitedPeopleDto> o = visitedPeopleService.byId(id, authIdHeader);
    if (o.isPresent()) {
      VisitedPeopleDto VisitedPeopleDto = o.get();
      long miliseconds = System.currentTimeMillis();
      Date date = new Date(miliseconds);

      VisitedPeopleDto.setDeleted(date);
      return ResponseEntity.status(HttpStatus.OK)
          .body(visitedPeopleService.save(VisitedPeopleDto, authIdHeader, id));
    }
    return ResponseEntity.notFound().build();
  }

  private ResponseEntity<Map<String, String>> validar(BindingResult result) {
    Map<String, String> errores = new HashMap<>();
    result
        .getFieldErrors()
        .forEach(
            err -> {
              errores.put(
                  err.getField(), "El campo " + err.getField() + " " + err.getDefaultMessage());
            });
    return ResponseEntity.badRequest().body(errores);
  }

  @GetMapping("/xSection/{idSection}")
  public ResponseEntity<?> xSection(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @PathVariable Integer idSection) {
    Map<String, Object> response = new HashMap<>();
    try {
      response.put(
          "xSection",
          visitedService.xSectionList(pageNo, pageSize, sortBy, orderBy, search, idSection));
      response.put("success", "true");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.OK);
    } catch (DataAccessException e) {
      response.put("mensaje", "Error al realizar la consulta a base de datos");
      response.put(
          "error", e.getMessage().concat(": ").concat(e.getMostSpecificCause().getMessage()));
      response.put("success", "false");
      return new ResponseEntity<Map<String, Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  @PutMapping("/createVisited")
  public ResponseEntity createVisited(
      @Valid @RequestBody VisitedDto request,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {
    if (result.hasErrors()) {
      return validar(result);
    }

    log.info("Creating visited with {}", request.toString());
    VisitedDto visited = visitedService.createVisited(request, authIdHeader);
    return ResponseEntity.ok(visited);
  }
}
