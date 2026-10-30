package com.electorama.msvc.elections.structure.service.rest;

import com.electorama.msvc.elections.structure.config.CustomFeignClientConfiguration;
import com.electorama.msvc.elections.structure.model.rest.elections.PollingBoothResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "${app.config.elections-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.elections-url}")
public interface ElectionsRestClient {

  @GetMapping("/pollingBooth/{pollingBoothId}")
  PollingBoothResponse getPollingBoothById(@PathVariable("pollingBoothId") long pollingBoothId);
}
