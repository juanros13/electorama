package com.electorama.msvc.electoralhistory.controller;

import com.electorama.msvc.electoralhistory.dto.TerritoryLevel;
import com.electorama.msvc.electoralhistory.dto.TrendReport;
import com.electorama.msvc.electoralhistory.service.ElectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Un cargo a lo largo de sus años, por municipio. Por el gateway: /api/electoral-history/v1/trend.
 */
@RestController
@RequestMapping("/v1/trend")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class TrendController {

  private final ElectionService elections;

  /**
   * @param office PRESIDENT, SENATOR, FEDERAL_DEPUTY, GOVERNOR, LOCAL_DEPUTY o MAYOR
   * @param groupBy bloc (por defecto: el bloque del año, comparable entre años), party o candidacy
   * @param level MUNICIPALITY, LOCAL_DISTRICT o FEDERAL_DISTRICT
   */
  @GetMapping
  public TrendReport trend(
      @RequestParam String office,
      @RequestParam(defaultValue = "bloc") String groupBy,
      @RequestParam(defaultValue = "MUNICIPALITY") TerritoryLevel level) {
    return elections.trend(office, groupBy, level);
  }
}
