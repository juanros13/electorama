package com.electorama.msvc.electionsresults.service;

import com.electorama.msvc.electionsresults.models.dto.ElectionAssociationIdsDto;
import com.electorama.msvc.electionsresults.models.dto.ResultDto;
import com.electorama.msvc.electionsresults.models.dto.UpdateElectionsResultsSheetsDto;
import java.util.List;

public interface ElectionsResultsSheetsService {

  void updateElectionsResultsSheets(List<UpdateElectionsResultsSheetsDto> updates);

  List<ResultDto> findElectionsAssociation(ElectionAssociationIdsDto electionAssociationIds);
}
