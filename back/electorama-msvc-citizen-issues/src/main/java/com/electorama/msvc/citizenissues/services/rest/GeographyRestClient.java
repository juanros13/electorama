package com.electorama.msvc.citizenissues.services.rest;

import com.electorama.msvc.citizenissues.config.CustomFeignClientConfiguration;
import com.electorama.msvc.citizenissues.model.rest.LocalityResponse;
import com.electorama.msvc.citizenissues.model.rest.MunicipalitiesResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "${app.config.geography-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.geography-url}")
public interface GeographyRestClient {

  @GetMapping("/municipality/{id}")
  MunicipalitiesResponse readMunicipalityById(@PathVariable("id") long id);

  @GetMapping("/municipality/byEntity/27")
  List<MunicipalitiesResponse> readMunicipalitiesByEntity();

  @GetMapping("/localitiesSection/{id}")
  LocalityResponse readLocalityById(@PathVariable("id") long id);

  @GetMapping("/localitiesSection/getAll")
  List<LocalityResponse> readLocalityAll();
}
