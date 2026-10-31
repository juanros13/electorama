package com.electorama.msvc.followers.service.rest;

import com.electorama.msvc.followers.config.CustomFeignClientConfiguration;
import com.electorama.msvc.followers.model.rest.EntitiesResponse;
import com.electorama.msvc.followers.model.rest.LocalityResponse;
import com.electorama.msvc.followers.model.rest.MunicipalitiesResponse;
import com.electorama.msvc.followers.model.rest.SeccionResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "${app.config.geography-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.geography-url}")
public interface GeographyRestClient {

  @GetMapping("/municipality/byEntity/27")
  List<MunicipalitiesResponse> readMunicipalitiesByEntity();

  @GetMapping("/seccion/byEntity/27")
  List<SeccionResponse> readSeccionByEntity();
}
