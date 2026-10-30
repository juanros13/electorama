package com.electorama.msvc.electionsresults.service;

import com.electorama.msvc.electionsresults.facade.ElectionResultFacade;
import com.electorama.msvc.electionsresults.models.dto.*;
import com.electorama.msvc.electionsresults.models.entity.ElectionResultEntity;
import com.electorama.msvc.electionsresults.models.mapper.ElectionResultMapper;
import com.electorama.msvc.electionsresults.models.repository.ElectionResultRepository;
import com.electorama.msvc.electionsresults.models.rest.PoliticalPartyResponse;
import com.electorama.msvc.electionsresults.models.rest.PollingBoothElectionAssociationResponse;
import com.electorama.msvc.electionsresults.service.rest.ElectionsRestClient;
import feign.FeignException;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ElectionResultServiceImpl implements ElectionResultService {
  @Autowired ElectionResultRepository electionResultRepository;

  @Autowired private ElectionsRestClient electionsRestClient;

  @Autowired ElectionResultFacade electionResultFacade;
  private final ElectionResultMapper electionResultMapper = new ElectionResultMapper();

  @Override
  public void updateElectionsResults(List<UpdateElectionResultsDto> updates) {

    List<Long> ElectionAssociationIds =
        updates.stream().map(UpdateElectionResultsDto::getElectionAssociationId).toList();

    for (Long electionAssociationId : ElectionAssociationIds) {
      electionsRestClient.updateSynchronized(electionAssociationId);
    }

    for (UpdateElectionResultsDto update : updates) {
      // Crea un ejemplo para buscar la entidad por politicarPartyId y electionAssociationId
      ElectionResultEntity electionResultEntity = new ElectionResultEntity();
      electionResultEntity.setPoliticalPartyId(update.getPoliticalPartyId());
      electionResultEntity.setElectionAssociationId(update.getElectionAssociationId());
      try {
        // Configura el matcher para que coincida exactamente con los valores de politicalPartyId y
        // electionAssociationId
        ExampleMatcher matcher =
            ExampleMatcher.matching()
                .withMatcher("politicalPartyId", ExampleMatcher.GenericPropertyMatcher::exact)
                .withMatcher("electionAssociationId", ExampleMatcher.GenericPropertyMatcher::exact);

        Optional<ElectionResultEntity> resultOptional =
            electionResultRepository.findOne(Example.of(electionResultEntity, matcher));

        if (resultOptional.isPresent()) {
          ElectionResultEntity resultToUpdate = resultOptional.get();
          resultToUpdate.setResult(update.getResult());
          electionResultRepository.save(resultToUpdate);
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
  public List<ElectionResultDto> findElectionsAssociation(
      ElectionAssociationIdsDto electionAssociationIds) {
    List<ElectionResultDto> dto =
        electionResultMapper.convertToDtoList(
            electionResultRepository.findByElectionAssociationIdIn(
                electionAssociationIds.getElectionAssociationIds()));
    return dto;
  }

  @Override
  public List<ResultDto> findElectionsAssociationB(
      ElectionAssociationIdsDto electionAssociationIds) {
    List<ResultDto> dto = new ArrayList<>();
    int count = 1;

    List<ElectionResultDto> electionResultDtoList =
        electionResultMapper.convertToDtoList(
            electionResultRepository.findByElectionAssociationIdIn(
                electionAssociationIds.getElectionAssociationIds()));

    ResultDto resultDto = new ResultDto();

    List<PoliticalPartyResponse> politicalPartyResponses = electionsRestClient.findAllElections();

    PollingBoothElectionAssociationResponse response = null;
    List<ElectionResultDto> electionResults = new ArrayList<>();
    Set<Long> idsSet = new HashSet<>();

    for (ElectionResultDto list : electionResultDtoList) {
      Long id = list.getPoliticalPartyId();
      if (idsSet.add(id)) { // add returns false if the element is already in the set
        response =
            electionsRestClient.readPollingBoothElectionAssociation(
                list.getElectionAssociationId());

        ElectionResultDto electionResult = new ElectionResultDto();
        electionResult.setId(list.getId());
        electionResult.setElectionAssociationId(list.getElectionAssociationId());
        electionResult.setCandidatePartyAssociationId(list.getCandidatePartyAssociationId());
        electionResult.setResult(list.getResult());
        electionResult.setPoliticalPartyId(list.getPoliticalPartyId());
        electionResult.setPoliticalPartyName(list.getPoliticalPartyName());
        electionResult.setElectionName(list.getElectionName());
        electionResult.setPollingBoothDescription(list.getPollingBoothDescription());
        electionResult.setSeccionNumber(list.getSeccionNumber());
        electionResult.setSeccionId(list.getSeccionId());
        electionResult.setMunicipalityId(list.getMunicipalityId());
        electionResult.setMunicipalityName(list.getMunicipalityName());
        electionResult.setDistrictFederalName(list.getDistrictFederalName());
        electionResult.setDistrictFederalId(list.getDistrictFederalId());
        electionResult.setDistrictLocalId(list.getDistrictLocalId());
        electionResult.setDistrictLocalName(list.getDistrictLocalName());
        electionResult.setCandidateId(list.getCandidateId());

        politicalPartyResponses.forEach(
            politicalParty -> {
              if (politicalParty.getId() == list.getPoliticalPartyId()) {
                electionResult.setSoftorder(politicalParty.getSoftorder());
              }
            });

        electionResults.add(electionResult);
        resultDto.setElectionResultDtoList(electionResults);
      }
    }
    resultDto.setId(Long.valueOf(count++));
    resultDto.setPollingBoothId(response != null ? response.getPollingBoothId() : null);
    resultDto.setPathSheet(response != null ? response.getPathSheet() : null);

    dto.add(resultDto);
    return dto;
  }

  @Override
  public List<PoliticalPartyResulDto> sumResultElectionsSheets(
      ElectionAssociationIdsDto electionAssociationIds) {
    StringJoiner joiner = new StringJoiner(", ");
    for (Long id : electionAssociationIds.getElectionAssociationIds()) {
      joiner.add(id.toString());
    }
    List<PoliticalPartyResulDto> dto =
        electionResultFacade.findByElectionsPositionsSheets(joiner.toString());
    return dto;
  }

  @Override
  public List<PoliticalPartyResulDto> sumResultElections(
      ElectionAssociationIdsDto electionAssociationIds) {
    StringJoiner joiner = new StringJoiner(", ");
    for (Long id : electionAssociationIds.getElectionAssociationIds()) {
      joiner.add(id.toString());
    }
    List<PoliticalPartyResulDto> dto =
        electionResultFacade.findByElectionsPositions(joiner.toString());
    return dto;
  }

  @Override
  public List<PoliticalPartyResulDto> sumResultElectionsMunicipality(
      MunicipalitiesIdsDto municipalitiesIdsDto) {
    StringJoiner joiner = new StringJoiner(", ");
    StringJoiner joinerCandidates = new StringJoiner(", ");
    for (Long id : municipalitiesIdsDto.getMunicipalitiesIds()) {
      joiner.add(id.toString());
    }
    for (Long id : municipalitiesIdsDto.getCandidatesIds()) {
      joinerCandidates.add(id.toString());
    }

    List<PoliticalPartyResulDto> dto =
        electionResultFacade.sumResultElectionsMunicipality(
            joinerCandidates.toString(), joiner.toString());
    return dto;
  }

  @Override
  public List<PoliticalPartyResulDto> sumResultElectionsDistrictFederal(
      FederalDistrictIdsDto federalDistrictIdsDto) {
    StringJoiner joiner = new StringJoiner(", ");
    StringJoiner joinerCandidates = new StringJoiner(", ");
    for (Long id : federalDistrictIdsDto.getFederalDistrictIds()) {
      joiner.add(id.toString());
    }
    for (Long id : federalDistrictIdsDto.getCandidatesIds()) {
      joinerCandidates.add(id.toString());
    }

    List<PoliticalPartyResulDto> dto =
        electionResultFacade.sumResultElectionsDistrictFederal(
            joinerCandidates.toString(), joiner.toString());
    return dto;
  }

  @Override
  public List<PoliticalPartyResulDto> sumResultElectionsDistrictLocal(
      LocalDistrictIdsDto localDistrictIdsDto) {
    StringJoiner joiner = new StringJoiner(", ");
    StringJoiner joinerCandidates = new StringJoiner(", ");
    for (Long id : localDistrictIdsDto.getLocalDistrictIds()) {
      joiner.add(id.toString());
    }
    for (Long id : localDistrictIdsDto.getCandidatesIds()) {
      joinerCandidates.add(id.toString());
    }

    List<PoliticalPartyResulDto> dto =
        electionResultFacade.sumResultElectionsDistrictLocal(
            joinerCandidates.toString(), joiner.toString());
    return dto;
  }
}
