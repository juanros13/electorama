package com.electorama.msvc.structure.controller;

import com.electorama.msvc.structure.model.projection.SupporterCoordinatorRow;
import com.electorama.msvc.structure.model.projection.SupporterProfileRow;
import com.electorama.msvc.structure.model.projection.SupporterSectionRow;
import com.electorama.msvc.structure.service.SupporterReportService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * El análisis de simpatizantes: agregados de sólo lectura por sección, por coordinador de zona y
 * del perfil de la encuesta de captura. Por el gateway: /api/structure/reports/supporters/…
 */
@RestController
@RequestMapping("/reports/supporters")
@RequiredArgsConstructor
public class SupporterReportController {

  private final SupporterReportService supporters;

  @GetMapping("/sections")
  public List<SupporterSectionRow> sections(@RequestParam(required = false) Long campaignId) {
    return supporters.sections(campaignId);
  }

  @GetMapping("/coordinators")
  public List<SupporterCoordinatorRow> coordinators(
      @RequestParam(required = false) Long campaignId) {
    return supporters.coordinators(campaignId);
  }

  @GetMapping("/profile")
  public List<SupporterProfileRow> profile(@RequestParam(required = false) Long campaignId) {
    return supporters.profile(campaignId);
  }
}
