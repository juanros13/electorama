package com.electorama.msvc.electoralhistory.controller;

import com.electorama.msvc.electoralhistory.dto.AnalysisReport;
import com.electorama.msvc.electoralhistory.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * El análisis para dirección de un año: dónde está el riesgo en ayuntamientos y diputaciones
 * locales. Por el gateway: /api/electoral-history/v1/analysis/2024.
 */
@RestController
@RequestMapping("/v1/analysis")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AnalysisController {

  private final AnalysisService analysis;

  @GetMapping("/{year}")
  public AnalysisReport analysis(@PathVariable int year) {
    return analysis.analysis(year);
  }
}
