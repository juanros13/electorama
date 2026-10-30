package com.electorama.googlecalendar.controller;

import com.electorama.googlecalendar.model.dto.Event;
import com.electorama.googlecalendar.service.EventService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
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
@RequestMapping(value = "/api/v1/event")
public class EventController {

  @Autowired private EventService eventService;

  @GetMapping
  public Map<String, Object> list(@RequestHeader("X-Auth-Id") String authIdHeader) {
    Map<String, Object> body = new HashMap<>();
    body.put("events", eventService.list(authIdHeader));
    return body;
  }

  @GetMapping("/{id}")
  public ResponseEntity<?> detail(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<Event> calendarOptional = eventService.byId(id, authIdHeader);
    if (calendarOptional.isPresent()) {
      return ResponseEntity.ok(calendarOptional.get());
    }
    return ResponseEntity.notFound().build();
  }

  @PostMapping
  public ResponseEntity<?> create(
      @Valid @RequestBody Event event,
      BindingResult result,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    return ResponseEntity.status(HttpStatus.CREATED).body(eventService.save(event, authIdHeader));
  }

  @PutMapping("/{id}")
  public ResponseEntity<?> edit(
      @Valid @RequestBody Event event,
      BindingResult result,
      @PathVariable Long id,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    if (result.hasErrors()) {
      return validar(result);
    }

    Optional<Event> o = eventService.byId(id, authIdHeader);
    if (o.isPresent()) {
      Event eventDB = o.get();

      eventDB.setCalendarId(event.getCalendarId());
      eventDB.setRecurringEventId(event.getRecurringEventId());
      eventDB.setIsFirstInstance(event.getIsFirstInstance());
      eventDB.setTitle(event.getTitle());
      eventDB.setDescription(event.getDescription());
      eventDB.setStart(event.getStart());
      eventDB.setEnd(event.getEnd());
      eventDB.setAllDay(event.getAllDay());
      eventDB.setRecurrence(event.getRecurrence());

      return ResponseEntity.status(HttpStatus.CREATED)
          .body(eventService.save(eventDB, authIdHeader));
    }
    return ResponseEntity.notFound().build();
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> delete(
      @PathVariable Long id, @RequestHeader("X-Auth-Id") String authIdHeader) {
    Optional<Event> o = eventService.byId(id, authIdHeader);
    if (o.isPresent()) {
      eventService.delete(id);
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
