package com.electorama.elections.services.rest;

import com.electorama.elections.config.CustomFeignClientConfiguration;
import com.electorama.elections.models.dto.ElectionAssociationIdsDto;
import com.electorama.elections.models.dto.FederalDistrictIdsDto;
import com.electorama.elections.models.dto.LocalDistrictIdsDto;
import com.electorama.elections.models.dto.MunicipalitiesIdsDto;
import com.electorama.elections.models.rest.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "${app.config.elections-results-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.elections-results-url}")
public interface ElectionsResultsRestClient {

  @PostMapping("/electionResult/findElectionsAssociation")
  List<ElectionResultResponse> findElectionsAssociationIn(
      @RequestBody ElectionAssociationIdsDto electionAssociationIdsDto);

  @GetMapping("/electionResult/sumResultElections")
  List<PoliticalPartyResulResponse> sumResultElectionsIn(
      @RequestBody ElectionAssociationIdsDto electionAssociationIdsDto);

  @GetMapping("/electionResult/sumResultElectionsSheets")
  List<PoliticalPartyResulResponse> sumResultElectionsSheetsIn(
      @RequestBody ElectionAssociationIdsDto electionAssociationIdsDto);

  @GetMapping("/electionResult/sumResultElectionsMunicipality")
  List<PoliticalPartyResulResponse> sumResultElectionsMunicipalityIn(
      @RequestBody MunicipalitiesIdsDto municipalitiesIdsDto);

  @GetMapping("/electionResult/sumResultElectionsDistrictFederal")
  List<PoliticalPartyResulResponse> sumResultElectionsDistrictFederalIn(
      @RequestBody FederalDistrictIdsDto federalDistrictIdsDto);

  @GetMapping("/electionResult/sumResultElectionsDistrictLocal")
  List<PoliticalPartyResulResponse> sumResultElectionsDistrictLocalIn(
      @RequestBody LocalDistrictIdsDto localDistrictIdsDto);
}
