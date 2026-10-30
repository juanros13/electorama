package com.electorama.msvc.followers.service.rest;

import com.electorama.msvc.followers.config.CustomFeignClientConfiguration;
import com.electorama.msvc.followers.model.rest.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "${app.config.elections-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.elections-url}")
public interface ElectionsRestClient {

  @GetMapping("/political-party/findAllPoliticalParties")
  List<PoliticalPartyResponse> findAllElections();
}
