package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.projection.FollowerCountRow;
import com.electorama.msvc.structure.service.ReportService;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Los reportes de captación.
 *
 * <p>Un solo endpoint con {@code groupBy} en vez de tres facades con la misma consulta copiada. El
 * ámbito se pasa como {@code scopeAuthId}: quien consulta ve lo que cuelga de sus puestos, sea
 * referente o coordinador de zona, y sin él es la vista de administrador.
 */
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

  private final ReportService reportService;

  @GetMapping("/followers")
  public List<FollowerCountRow> followers(
      @RequestParam(defaultValue = "MUNICIPALITY") ReportService.GroupBy groupBy,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime to,
      @RequestParam(required = false) String scopeAuthId,
      @RequestParam(required = false) Long campaignId) {
    return reportService.followers(groupBy, from, to, scopeAuthId, campaignId);
  }

  @GetMapping("/mobilization")
  public Map<String, Long> mobilization(
      @RequestParam(required = false) Integer sectionGeoId,
      @RequestParam(required = false) Integer sectionNumber,
      @RequestParam(required = false) Long campaignId) {
    return reportService.mobilization(sectionGeoId, sectionNumber, campaignId);
  }
}
