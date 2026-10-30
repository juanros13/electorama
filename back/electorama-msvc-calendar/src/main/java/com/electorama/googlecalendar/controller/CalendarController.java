package com.electorama.googlecalendar.controller;

import com.electorama.googlecalendar.model.dto.Calendar;
import com.electorama.googlecalendar.service.CalendarService;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/calendar")
public class CalendarController {

  @Autowired private CalendarService calendarService;

  @GetMapping
  public Map<String, Object> list(@RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> body = new HashMap<>();
    body.put("calendars", calendarService.list(authIdHeader));
    return body;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<Calendar> calendarOptional = calendarService.byId(id, authIdHeader);
    if (calendarOptional.isPresent()) {
      return ResponseEntity.ok(calendarOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping
  public ResponseEntity<?> create(
      @Valid @RequestBody Calendar calendario,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(calendarService.save(calendario, authIdHeader));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> edit(
      @Valid @RequestBody Calendar calendar,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    Optional<Calendar> o = calendarService.byId(id, authIdHeader);
    if (o.isPresent()) {
      Calendar calendarDB = o.get();

      calendarDB.setTitle(calendar.getTitle());
      calendarDB.setColor(calendar.getColor());
      calendarDB.setVisible(calendar.getVisible());
      return ResponseEntity.status(HttpStatus.CREATED)
          .body(calendarService.save(calendarDB, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<Calendar> o = calendarService.byId(id, authIdHeader);
    if (o.isPresent()) {
      calendarService.delete(id);
      return ResponseEntity.noContent().build();
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
}
