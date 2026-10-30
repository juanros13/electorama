package com.electorama.msvc.visits.controller;

import com.electorama.msvc.visits.model.dto.VisitedPeopleDto;
import com.electorama.msvc.visits.service.VisitedPeopleService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/visitedPeople")
public class VisitedPeopleController {

  @Autowired VisitedPeopleService visitedPeopleService;

  @GetMapping()
  public ResponseEntity<?> listPeople(
      @RequestParam(defaultValue = "0") Integer pageNo,
      @RequestParam(defaultValue = "30") Integer pageSize,
      @RequestParam(defaultValue = "id") String sortBy,
      @RequestParam(defaultValue = "desc") String orderBy,
      @RequestParam(defaultValue = "") String search,
      @RequestHeader("X-Auth-Id") String authIdHeader,
      HttpServletRequest servletRequest) {
    Map<String, Object> response = new HashMap<>();
    try {
      if (servletRequest.isUserInRole("ADMIN")) {

        response.put(
            "visitedPeople",
            visitedPeopleService.listPeopleAll(pageNo, pageSize, sortBy, orderBy, search));
      } else {
        response.put(
            "visitedPeople",
            visitedPeopleService.listPeople(
                pageNo, pageSize, sortBy, orderBy, search, authIdHeader));
      }
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

  @PutMapping("/addVisit/{id}")
  public ResponseEntity<?> edit(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {

    Optional<VisitedPeopleDto> visitDto = visitedPeopleService.byId(id, authIdHeader);
    if (visitDto.isPresent()) {
      VisitedPeopleDto visitedDB = visitDto.get();

      if (visitedDB.getVisitNumber() == 8) {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
      }
      visitedDB.setVisitNumber(visitedDB.getVisitNumber() + 1);

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(visitedPeopleService.addVisit(visitedDB, authIdHeader, id));
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<VisitedPeopleDto> visitedPeopleDto = visitedPeopleService.byId(id, authIdHeader);
    if (visitedPeopleDto.isPresent()) {
      return ResponseEntity.ok(visitedPeopleDto.get());
    }
    return ResponseEntity.notFound().build();
  }
}
