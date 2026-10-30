package com.electorama.msvc.structure.service.rest;

import java.util.Map;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * El catálogo territorial vive en msvc-geography (con PostGIS). Se llama con el token de quien
 * captura ({@link com.electorama.msvc.structure.config.FeignAuthForwarding}).
 */
@FeignClient(name = "msvc-geography-catalog", url = "${app.config.geography-url}")
public interface GeographyClient {

  /**
   * {@code inside}: true si el punto cae en la sección o a no más de {@code tolerance} metros de su
   * borde; null si la sección no tiene geometría.
   */
  @GetMapping("/catalog/sections/{id}/contains")
  Map<String, Object> sectionContains(
      @PathVariable("id") Integer sectionGeoId,
      @RequestParam("lat") double lat,
      @RequestParam("lng") double lng,
      @RequestParam("tolerance") double toleranceMeters);
}
