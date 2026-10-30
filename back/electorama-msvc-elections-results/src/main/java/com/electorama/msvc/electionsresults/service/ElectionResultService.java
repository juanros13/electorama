package com.electorama.msvc.electionsresults.service;

import com.electorama.msvc.electionsresults.models.dto.*;
import java.util.List;

public interface ElectionResultService {

  void updateElectionsResults(List<UpdateElectionResultsDto> updates);

  List<ElectionResultDto> findElectionsAssociation(
      ElectionAssociationIdsDto electionAssociationIds);

  List<ResultDto> findElectionsAssociationB(ElectionAssociationIdsDto electionAssociationIds);

  List<PoliticalPartyResulDto> sumResultElections(ElectionAssociationIdsDto electionAssociationIds);

  List<PoliticalPartyResulDto> sumResultElectionsSheets(
      ElectionAssociationIdsDto electionAssociationIds);

  List<PoliticalPartyResulDto> sumResultElectionsMunicipality(
      MunicipalitiesIdsDto municipalitiesIdsDto);

  List<PoliticalPartyResulDto> sumResultElectionsDistrictFederal(
      FederalDistrictIdsDto federalDistrictIdsDto);

  List<PoliticalPartyResulDto> sumResultElectionsDistrictLocal(
      LocalDistrictIdsDto localDistrictIdsDto);
}
