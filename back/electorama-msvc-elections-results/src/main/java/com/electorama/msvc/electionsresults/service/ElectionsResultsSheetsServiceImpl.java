package com.electorama.msvc.electionsresults.service;

import com.electorama.msvc.electionsresults.models.dto.*;
import com.electorama.msvc.electionsresults.models.entity.ElectionResultsSheetsEntity;
import com.electorama.msvc.electionsresults.models.mapper.ElectionsResultsSheetsMapper;
import com.electorama.msvc.electionsresults.models.repository.ElectionsResultsSheetsRepository;
import com.electorama.msvc.electionsresults.models.rest.PoliticalPartyResponse;
import com.electorama.msvc.electionsresults.models.rest.PollingBoothElectionAssociationResponse;
import com.electorama.msvc.electionsresults.service.rest.ElectionsRestClient;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

@Service
public class ElectionsResultsSheetsServiceImpl implements ElectionsResultsSheetsService {
  @Autowired ElectionsResultsSheetsRepository electionsResultsSheetsRepository;

  @Autowired private ElectionsRestClient electionsRestClient;

  private final ElectionsResultsSheetsMapper electionsResultsSheetsMapper =
      new ElectionsResultsSheetsMapper();

  @Override
  public void updateElectionsResultsSheets(List<UpdateElectionsResultsSheetsDto> updates) {

    List<Long> ElectionAssociationIds =
        updates.stream().map(UpdateElectionsResultsSheetsDto::getElectionAssociationId).toList();
    /****
     * for (Long electionAssociationId : ElectionAssociationIds) {
     * electionsRestClient.updateSynchronized(electionAssociationId);
     * }
     ****/
    for (UpdateElectionsResultsSheetsDto update : updates) {

      ElectionResultsSheetsEntity electionResultsSheetsEntity = new ElectionResultsSheetsEntity();
      electionResultsSheetsEntity.setPoliticalPartyId(update.getPoliticalPartyId());
      electionResultsSheetsEntity.setElectionAssociationId(update.getElectionAssociationId());
      try {
        ExampleMatcher matcher =
            ExampleMatcher.matching()
                .withMatcher("politicalPartyId", ExampleMatcher.GenericPropertyMatcher::exact)
                .withMatcher("electionAssociationId", ExampleMatcher.GenericPropertyMatcher::exact);

        Optional<ElectionResultsSheetsEntity> resultOptional =
            electionsResultsSheetsRepository.findOne(
                Example.of(electionResultsSheetsEntity, matcher));

        if (resultOptional.isPresent()) {
          ElectionResultsSheetsEntity resultToUpdate = resultOptional.get();
          resultToUpdate.setResult(update.getResult());
          electionsResultsSheetsRepository.save(resultToUpdate);
        } else {
          throw new EntityNotFoundException(
              "No se encontró información para los ids proporcionados");
        }
      } catch (FeignException fe) {
        fe.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public List<ResultDto> findElectionsAssociation(
      ElectionAssociationIdsDto electionAssociationIds) {
    List<ResultDto> dto = new ArrayList<>();
    int count = 1;

    List<ElectionsResultsSheetsDto> electionResultDtoList =
        electionsResultsSheetsMapper.convertToDtoList(
            electionsResultsSheetsRepository.findByElectionAssociationIdIn(
                electionAssociationIds.getElectionAssociationIds()));

    ResultDto resultDto = new ResultDto();
    List<PoliticalPartyResponse> politicalPartyResponses = electionsRestClient.findAllElections();

    PollingBoothElectionAssociationResponse response = null;
    List<ElectionResultDto> electionResults = new ArrayList<>();
    for (ElectionsResultsSheetsDto list : electionResultDtoList) {

      response =
          electionsRestClient.readPollingBoothElectionAssociation(list.getElectionAssociationId());

      ElectionResultDto electionResult = new ElectionResultDto();
      electionResult.setId(list.getId());
      electionResult.setElectionAssociationId(list.getElectionAssociationId());
      electionResult.setCandidatePartyAssociationId(list.getCandidatePartyAssociationId());
      electionResult.setResult(list.getResult());
      electionResult.setPoliticalPartyId(list.getPoliticalPartyId());
      electionResult.setPoliticalPartyName(list.getPoliticalPartyName());
      electionResult.setElectionName(list.getElectionName());
      electionResult.setPollingBoothDescription(list.getPollingBoothDescription());
      electionResult.setSeccionId(list.getSeccionId());
      electionResult.setMunicipalityId(list.getMunicipalityId());
      electionResult.setMunicipalityName(list.getMunicipalityName());
      electionResult.setDistrictFederalId(list.getDistrictFederalId());
      electionResult.setDistrictLocalId(list.getDistrictLocalId());
      electionResult.setDistrictLocalName(list.getDistrictLocalName());

      politicalPartyResponses.forEach(
          politicalParty -> {
            if (politicalParty.getId() == list.getPoliticalPartyId()) {
              electionResult.setSoftorder(politicalParty.getSoftorder());
            }
          });

      electionResults.add(electionResult);
      resultDto.setElectionResultDtoList(electionResults);
    }
    resultDto.setId(Long.valueOf(count++));
    resultDto.setPollingBoothId(response != null ? response.getPollingBoothId() : null);
    resultDto.setPathSheet(response != null ? response.getPathSheet() : null);

    dto.add(resultDto);
    return dto;
  }
}
