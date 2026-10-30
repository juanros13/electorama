package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.dto.CaptureRequests.Capture;
import com.electorama.msvc.structure.model.dto.CaptureRequests.CaptureTarget;
import com.electorama.msvc.structure.model.dto.CaptureRequests.Captured;
import com.electorama.msvc.structure.model.dto.PagedResponse;
import com.electorama.msvc.structure.model.projection.FollowerRow;
import com.electorama.msvc.structure.model.projection.MembershipView;
import com.electorama.msvc.structure.model.repository.SupporterListRepository;
import com.electorama.msvc.structure.service.CaptureService;
import com.electorama.msvc.structure.service.MembershipService;
import com.electorama.msvc.structure.service.PositionService;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** El convencido. */
@RestController
@RequestMapping("/memberships")
@RequiredArgsConstructor
public class MembershipController {

  private final MembershipService membershipService;
  private final CaptureService captureService;
  private final SupporterListRepository supporterList;
  private final PositionService positionService;

  /**
   * Los convencidos del puesto y de todo lo que cuelga de él: buscar (nombre, CURP o teléfono) y
   * filtrar (simpatiza, comité, representante de casilla, con ubicación, rango de fechas).
   */
  @GetMapping("/search")
  @PreAuthorize("@branch.canRead(#positionId)")
  public SupporterListRepository.Page search(
      @RequestParam Long positionId,
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String supports,
      @RequestParam(required = false) Boolean committee,
      @RequestParam(required = false) Boolean pollingRep,
      @RequestParam(required = false) Boolean withLocation,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "25") int size) {
    return supporterList.search(
        positionId,
        new SupporterListRepository.Filter(
            q, supports, committee, pollingRep, withLocation, from, to),
        Math.max(page, 0),
        Math.min(Math.max(size, 1), 200));
  }

  /** Las tarjetas de arriba: la meta del puesto y cuántos lleva, hoy, en 7 días, etc. */
  @GetMapping("/summary")
  @PreAuthorize("@branch.canRead(#positionId)")
  public Map<String, Object> summary(@RequestParam Long positionId) {
    SupporterListRepository.Summary s = supporterList.summary(positionId);
    Integer goal = positionService.get(positionId).getGoal();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("positionId", positionId);
    out.put("goal", goal == null ? 0 : goal);
    out.put("total", s.total());
    out.put("today", s.today());
    out.put("last7", s.last7());
    out.put("supportsYes", s.supportsYes());
    out.put("withPhone", s.withPhone());
    out.put("withLocation", s.withLocation());
    out.put("committee", s.committee());
    out.put("pollingRep", s.pollingRep());
    return out;
  }

  /**
   * Captura un convencido en un puesto de la rama de quien llama (por omisión, el suyo). 201 con el
   * puesto donde quedó; 409 ALREADY_SUPPORTER si ya es convencido de otro puesto en esa estructura.
   */
  @PostMapping
  @PreAuthorize("@branch.isAdmin() or @branch.isStructureMember()")
  public ResponseEntity<Captured> capture(@RequestBody Capture request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(captureService.capture(request));
  }

  /** Dónde puede dejar convencidos quien llama: sus puestos y los de su rama con titular. */
  @GetMapping("/capture-targets")
  @PreAuthorize("@branch.isAdmin() or @branch.isStructureMember()")
  public List<CaptureTarget> captureTargets() {
    return captureService.targets();
  }

  @GetMapping("/{id}")
  public MembershipView get(@PathVariable Long id) {
    return membershipService.get(id);
  }

  @GetMapping
  @PreAuthorize("@branch.canRead(#positionId)")
  public List<MembershipView> byPosition(
      @RequestParam Long positionId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size) {
    return membershipService.byPosition(positionId, page, size);
  }

  /**
   * Los convencidos de un coordinador de zona, opcionalmente de un solo promotor.
   *
   * <p>Es el endpoint que sustituye al `followers ⋈ promoters` que `msvc-followers` hacía contra su
   * propia base.
   */
  @GetMapping("/by-coordinator/{authId}")
  public PagedResponse<FollowerRow> byCoordinator(
      @PathVariable String authId,
      @RequestParam(required = false) String promoterAuthId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "100") int size,
      @RequestParam(required = false) Long campaignId) {
    return membershipService.byCoordinator(authId, promoterAuthId, page, size, campaignId);
  }

  /** Traslada al convencido a otro puesto, conservando su linaje. */
  @PostMapping("/{id}/transfer")
  @PreAuthorize("hasRole('ADMIN')")
  public MembershipView transfer(@PathVariable Long id, @RequestParam Long positionId) {
    return membershipService.transfer(id, positionId);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> close(@PathVariable Long id) {
    membershipService.close(id);
    return ResponseEntity.noContent().build();
  }
}
