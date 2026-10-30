package com.electorama.msvc.elections.structure.service.rest;

import com.electorama.msvc.elections.structure.config.CustomFeignClientConfiguration;
import com.electorama.msvc.elections.structure.model.rest.EntitiesResponse;
import com.electorama.msvc.elections.structure.model.rest.LocalityResponse;
import com.electorama.msvc.elections.structure.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.elections.structure.model.rest.SeccionResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "${app.config.geography-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.geography-url}")
public interface GeographyRestClient {

  @GetMapping("/municipality")
  List<MunicipalitiesResponse> readMunicipalities();

  @GetMapping("/municipality/byEntity/27")
  List<MunicipalitiesResponse> readMunicipalitiesByEntity();

  @GetMapping("/seccion/byEntity/27")
  List<SeccionResponse> readSeccionByEntity();

  @GetMapping("/seccion/{id}")
  SeccionResponse readSeccionById(@PathVariable("id") long id);

  @GetMapping("/municipality/{id}")
  MunicipalitiesResponse readMunicipalityById(@PathVariable("id") long id);

  @GetMapping("/localitiesSection/{id}")
  LocalityResponse readLocalityById(@PathVariable("id") long id);

  @GetMapping("/entity/")
  List<EntitiesResponse> readAllEntities();
}
