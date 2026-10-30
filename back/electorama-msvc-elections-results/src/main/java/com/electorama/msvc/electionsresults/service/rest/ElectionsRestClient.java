package com.electorama.msvc.electionsresults.service.rest;

import com.electorama.msvc.electionsresults.config.CustomFeignClientConfiguration;
import com.electorama.msvc.electionsresults.models.rest.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "${app.config.elections-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.elections-url}")
public interface ElectionsRestClient {
  @PostMapping("/pollingBoothElectionAssociation/updateSynchronized/{id}")
  void updateSynchronized(@PathVariable("id") Long id);

  @GetMapping("/pollingBoothElectionAssociation/getPollingBoothsElectionAssociation/{id}")
  PollingBoothElectionAssociationResponse readPollingBoothElectionAssociation(
      @PathVariable("id") Long id);

  @GetMapping("/political-party/findAllPoliticalParties")
  List<PoliticalPartyResponse> findAllElections();
}
