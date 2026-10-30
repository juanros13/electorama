package com.electorama.elections.services;

import com.electorama.elections.facade.ElectionResultFacade;
import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.entity.PoliticalPartyEntity;
import com.electorama.elections.models.enums.ElectionStatusEnum;
import com.electorama.elections.models.mapper.ElectionMapper;
import com.electorama.elections.models.mapper.ElectionVoterListMapper;
import com.electorama.elections.models.repository.ElectionRepository;
import com.electorama.elections.models.repository.ElectionVoterListRepository;
import com.electorama.elections.models.repository.PoliticalPartyRepository;
import com.electorama.elections.models.rest.PoliticalPartyResulResponse;
import com.electorama.elections.models.rest.UserFederalDistrictResponse;
import com.electorama.elections.models.rest.UserLocalDistrictResponse;
import com.electorama.elections.models.rest.UserMunicipalityResponse;
import com.electorama.elections.services.rest.ElectionsResultsRestClient;
import com.electorama.elections.services.rest.UsersRestClient;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ElectionServiceImpl implements ElectionService {
  @Autowired ElectionRepository electionRepository;

  @Autowired PoliticalPartyRepository politicalPartyRepository;
  private final ElectionMapper electionMapper = new ElectionMapper();

  private final ElectionVoterListMapper electionVoterListMapper = new ElectionVoterListMapper();
  @Autowired ElectionResultFacade electionResultFacade;
  @Autowired private ElectionsResultsRestClient electionsResultsRestClient;

  @Autowired private UsersRestClient usersRestClient;

  @Autowired private ElectionVoterListRepository electionVoterListRepository;

  @Override
  public List<ElectionDto> findAllElections() {
    return new ArrayList<>(electionMapper.convertToDto(electionRepository.findAll()));
  }

  @Override
  public List<ElectionDto> findByStatus(String status) {
    ElectionStatusEnum statusEnum = null;

    if (status.equalsIgnoreCase("ACTUAL")) {
      statusEnum = ElectionStatusEnum.ACTUAL;
    } else if (status.equalsIgnoreCase("DEFAULT")) {
      statusEnum = ElectionStatusEnum.DEFAULT;
    } else if (status.equalsIgnoreCase("HISTORICAL")) {
      statusEnum = ElectionStatusEnum.HISTORICAL;
    }
    return new ArrayList<>(
        electionMapper.convertToDto(electionRepository.findByStatus(statusEnum)));
  }

  @Override
  public List<ElectionDto> findByAnioElection(String anio) {
    return new ArrayList<>(
        electionMapper.convertToDto(electionRepository.findByElectionYear_Name(anio)));
  }

  @Override
  public List<ElectionDto> findByPosition(Integer positionId) {
    return new ArrayList<>(
        electionMapper.convertToDto(electionRepository.findByElectionPosition_Id(positionId)));
  }

  @Override
  public List<ElectionDto> findByType(Integer typeId) {
    return new ArrayList<>(
        electionMapper.convertToDto(electionRepository.findByElectionType_Id(typeId)));
  }

  @Override
  public List<ElectionDto> getElections(
      Long electionYear, Long electionPosition, Long electionType) {

    if (electionYear > 0 && electionPosition == 0 && electionType == 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(electionRepository.findByElectionYear_Id(electionYear)));
    } else if (electionYear > 0 && electionPosition > 0 && electionType == 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(
              electionRepository.findByElectionYear_IdAndElectionPosition_Id(
                  electionYear, electionPosition)));
    } else if (electionYear > 0 && electionPosition > 0 && electionType > 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(
              electionRepository.findByElectionYear_IdAndElectionPosition_IdAndElectionType_Id(
                  electionYear, electionPosition, electionType)));
    } else if (electionYear == 0 && electionPosition > 0 && electionType == 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(
              electionRepository.findByElectionPosition_Id(electionPosition)));
    } else if (electionYear == 0 && electionPosition == 0 && electionType > 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(electionRepository.findByElectionType_Id(electionType)));
    } else if (electionYear > 0 && electionPosition == 0 && electionType > 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(
              electionRepository.findByElectionYear_IdAndElectionType_Id(
                  electionYear, electionType)));
    } else if (electionYear == 0 && electionPosition > 0 && electionType > 0) {
      return new ArrayList<>(
          electionMapper.convertToDto(
              electionRepository.findByElectionPosition_IdAndElectionType_Id(
                  electionPosition, electionType)));
    } else {
      return null;
    }
  }

  @Override
  public List<ElectionsAndCandidatesDto> electionResult(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest) {
    List<ElectionsAndCandidatesDto> dtoList = new ArrayList<>();
    String roleName = getRoleName(servletRequest);
    ElectionAssociationIdsDto electionAssociationIdsDto = new ElectionAssociationIdsDto();
    MunicipalitiesIdsDto municipalitiesIdsDto = new MunicipalitiesIdsDto();
    FederalDistrictIdsDto federalDistrictIdsDto = new FederalDistrictIdsDto();
    LocalDistrictIdsDto localDistrictIdsDto = new LocalDistrictIdsDto();
    List<Long> geographyIds = new ArrayList<>();
    List<ElectionsAndCandidatesDto> resultList = null;

    if (roleName.equalsIgnoreCase("ADMIN")) {
      resultList = electionResultFacade.findByElectionsPositions(electionsPositionsId);
    } else if (roleName.equalsIgnoreCase("MUNICIPALITY_TRACKER")) {
      List<UserMunicipalityResponse> userMunicipalityResponses =
          usersRestClient.readUserAllCoorMunWithIdMun();
      geographyIds =
          userMunicipalityResponses.stream()
              .map(
                  municipalityResponse -> {
                    return Long.valueOf(municipalityResponse.getMunicipalityId());
                  })
              .collect(Collectors.toList());

      resultList =
          electionResultFacade.findByElectionsMunicipality(electionsPositionsId, geographyIds);
    } else if (roleName.equalsIgnoreCase("LOCAL_DISTRICT_TRACKER")) {
      List<UserLocalDistrictResponse> userLocalDistrictResponses =
          usersRestClient.readUserLocalDistrict();

      geographyIds =
          userLocalDistrictResponses.stream()
              .map(
                  localDistrictResponse -> {
                    return Long.valueOf(localDistrictResponse.getLocalDistrictId());
                  })
              .collect(Collectors.toList());

      resultList =
          electionResultFacade.findByElectionsLocalDistrict(electionsPositionsId, geographyIds);
    } else if (roleName.equalsIgnoreCase("FEDERAL_DISTRICT_TRACKER")) {
      List<UserFederalDistrictResponse> userFederalDistrictResponses =
          usersRestClient.readUserFederalDistrict();
      geographyIds =
          userFederalDistrictResponses.stream()
              .map(
                  federalDistrictResponse -> {
                    return Long.valueOf(federalDistrictResponse.getFederalDistrictId());
                  })
              .collect(Collectors.toList());
      resultList =
          electionResultFacade.findByElectionsFederalDistrict(electionsPositionsId, geographyIds);
    }
    List<Long> ids =
        resultList.stream()
            .map(ElectionsAndCandidatesDto::getCandidateId)
            .collect(Collectors.toList());

    electionAssociationIdsDto.setElectionAssociationIds(ids);
    List<PoliticalPartyResulResponse> response = new ArrayList<>();
    List<PoliticalPartyEntity> political = politicalPartyRepository.findAll();

    // Crear un mapa de politicalPartyName a PoliticalPartyEntity
    Map<String, PoliticalPartyEntity> politicalPartyMap =
        political.stream()
            .collect(Collectors.toMap(PoliticalPartyEntity::getAcronym, party -> party));

    if (roleName.equalsIgnoreCase("ADMIN")) {
      log.info("Admin: ");
      response = electionsResultsRestClient.sumResultElectionsIn(electionAssociationIdsDto);
    } else if (roleName.equalsIgnoreCase("MUNICIPALITY_TRACKER")) {
      log.info("Municipality Tracker: ");
      List<UserMunicipalityResponse> userMunicipalityResponses =
          usersRestClient.readUserAllCoorMunWithIdMun();
      geographyIds =
          userMunicipalityResponses.stream()
              .map(
                  municipalityResponse -> {
                    return Long.valueOf(municipalityResponse.getMunicipalityId());
                  })
              .collect(Collectors.toList());
      municipalitiesIdsDto.setCandidatesIds(ids);
      municipalitiesIdsDto.setMunicipalitiesIds(geographyIds);

      response = electionsResultsRestClient.sumResultElectionsMunicipalityIn(municipalitiesIdsDto);

    } else if (roleName.equalsIgnoreCase("LOCAL_DISTRICT_TRACKER")) {
      log.info("Local District Tracker: ");
      List<UserLocalDistrictResponse> userLocalDistrictResponses =
          usersRestClient.readUserLocalDistrict();

      geographyIds =
          userLocalDistrictResponses.stream()
              .map(
                  localDistrictResponse -> {
                    return Long.valueOf(localDistrictResponse.getLocalDistrictId());
                  })
              .collect(Collectors.toList());
      localDistrictIdsDto.setCandidatesIds(ids);
      localDistrictIdsDto.setLocalDistrictIds(geographyIds);

      response = electionsResultsRestClient.sumResultElectionsDistrictLocalIn(localDistrictIdsDto);
    } else if (roleName.equalsIgnoreCase("FEDERAL_DISTRICT_TRACKER")) {
      log.info("Federal District Tracker: ");
      List<UserFederalDistrictResponse> userFederalDistrictResponses =
          usersRestClient.readUserFederalDistrict();
      geographyIds =
          userFederalDistrictResponses.stream()
              .map(
                  federalDistrictResponse -> {
                    return Long.valueOf(federalDistrictResponse.getFederalDistrictId());
                  })
              .collect(Collectors.toList());
      federalDistrictIdsDto.setCandidatesIds(ids);
      federalDistrictIdsDto.setFederalDistrictIds(geographyIds);

      response =
          electionsResultsRestClient.sumResultElectionsDistrictFederalIn(federalDistrictIdsDto);
    }

    for (ElectionsAndCandidatesDto result : resultList) {
      int totalVotes = 0; // Variable para almacenar la suma de los resultados
      List<ResultDto> resultDtoList = new ArrayList<>();
      ElectionsAndCandidatesDto resultDto = new ElectionsAndCandidatesDto();
      for (PoliticalPartyResulResponse resp : response) {
        if (resp.getCandidateId().equals(result.getCandidateId())) {
          ResultDto electionResult = new ResultDto();
          electionResult.setCandidateId(resp.getCandidateId());
          electionResult.setPoliticalPartyName(resp.getPoliticalPartyName());
          electionResult.setResult(resp.getResult());

          // Buscar el softorder basado en el acronym
          PoliticalPartyEntity partyEntity = politicalPartyMap.get(resp.getPoliticalPartyName());
          if (partyEntity != null) {
            electionResult.setSoftorder(partyEntity.getSoftorder());
          }

          resultDtoList.add(electionResult);
          // Sumar el valor de result
          totalVotes += resp.getResult();
        }
      }
      resultDto.setCandidateId(result.getCandidateId());
      resultDto.setResultList(resultDtoList);
      resultDto.setTotalVotes(totalVotes);
      resultDto.setElectionPositionName(result.getElectionPositionName());
      resultDto.setCandidatePath(result.getCandidatePath());
      resultDto.setElectionName(result.getElectionName());
      resultDto.setNames(result.getNames());
      resultDto.setLastNames(result.getLastNames());
      resultDto.setSurNames(result.getSurNames());
      resultDto.setElectionId(result.getElectionId());

      dtoList.add(resultDto);
    }
    return dtoList;
  }

  @Override
  public List<ElectionsAndCandidatesDto> electionResultSheets(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest) {
    List<ElectionsAndCandidatesDto> dtoList = new ArrayList<>();
    String roleName = getRoleName(servletRequest);
    ElectionAssociationIdsDto electionAssociationIdsDto = new ElectionAssociationIdsDto();
    MunicipalitiesIdsDto municipalitiesIdsDto = new MunicipalitiesIdsDto();
    FederalDistrictIdsDto federalDistrictIdsDto = new FederalDistrictIdsDto();
    LocalDistrictIdsDto localDistrictIdsDto = new LocalDistrictIdsDto();
    List<Long> geographyIds = new ArrayList<>();
    List<ElectionsAndCandidatesDto> resultList = null;

    if (roleName.equalsIgnoreCase("ADMIN")) {
      resultList = electionResultFacade.findByElectionsPositions(electionsPositionsId);
    } else if (roleName.equalsIgnoreCase("MUNICIPALITY_TRACKER")) {
      List<UserMunicipalityResponse> userMunicipalityResponses =
          usersRestClient.readUserAllCoorMunWithIdMun();
      geographyIds =
          userMunicipalityResponses.stream()
              .map(
                  municipalityResponse -> {
                    return Long.valueOf(municipalityResponse.getMunicipalityId());
                  })
              .collect(Collectors.toList());

      resultList =
          electionResultFacade.findByElectionsMunicipality(electionsPositionsId, geographyIds);
    } else if (roleName.equalsIgnoreCase("LOCAL_DISTRICT_TRACKER")) {
      List<UserLocalDistrictResponse> userLocalDistrictResponses =
          usersRestClient.readUserLocalDistrict();

      geographyIds =
          userLocalDistrictResponses.stream()
              .map(
                  localDistrictResponse -> {
                    return Long.valueOf(localDistrictResponse.getLocalDistrictId());
                  })
              .collect(Collectors.toList());

      resultList =
          electionResultFacade.findByElectionsLocalDistrict(electionsPositionsId, geographyIds);
    } else if (roleName.equalsIgnoreCase("FEDERAL_DISTRICT_TRACKER")) {
      List<UserFederalDistrictResponse> userFederalDistrictResponses =
          usersRestClient.readUserFederalDistrict();
      geographyIds =
          userFederalDistrictResponses.stream()
              .map(
                  federalDistrictResponse -> {
                    return Long.valueOf(federalDistrictResponse.getFederalDistrictId());
                  })
              .collect(Collectors.toList());
      resultList =
          electionResultFacade.findByElectionsFederalDistrict(electionsPositionsId, geographyIds);
    }
    List<Long> ids =
        resultList.stream()
            .map(ElectionsAndCandidatesDto::getCandidateId)
            .collect(Collectors.toList());

    electionAssociationIdsDto.setElectionAssociationIds(ids);
    List<PoliticalPartyResulResponse> response = new ArrayList<>();
    List<PoliticalPartyEntity> political = politicalPartyRepository.findAll();

    // Crear un mapa de politicalPartyName a PoliticalPartyEntity
    Map<String, PoliticalPartyEntity> politicalPartyMap =
        political.stream()
            .collect(Collectors.toMap(PoliticalPartyEntity::getAcronym, party -> party));

    if (roleName.equalsIgnoreCase("ADMIN")) {
      log.info("Admin: ");
      response = electionsResultsRestClient.sumResultElectionsSheetsIn(electionAssociationIdsDto);
    } else if (roleName.equalsIgnoreCase("MUNICIPALITY_TRACKER")) {
      log.info("Municipality Tracker: ");
      List<UserMunicipalityResponse> userMunicipalityResponses =
          usersRestClient.readUserAllCoorMunWithIdMun();
      geographyIds =
          userMunicipalityResponses.stream()
              .map(
                  municipalityResponse -> {
                    return Long.valueOf(municipalityResponse.getMunicipalityId());
                  })
              .collect(Collectors.toList());
      municipalitiesIdsDto.setCandidatesIds(ids);
      municipalitiesIdsDto.setMunicipalitiesIds(geographyIds);

      response = electionsResultsRestClient.sumResultElectionsMunicipalityIn(municipalitiesIdsDto);

    } else if (roleName.equalsIgnoreCase("LOCAL_DISTRICT_TRACKER")) {
      log.info("Local District Tracker: ");
      List<UserLocalDistrictResponse> userLocalDistrictResponses =
          usersRestClient.readUserLocalDistrict();

      geographyIds =
          userLocalDistrictResponses.stream()
              .map(
                  localDistrictResponse -> {
                    return Long.valueOf(localDistrictResponse.getLocalDistrictId());
                  })
              .collect(Collectors.toList());
      localDistrictIdsDto.setCandidatesIds(ids);
      localDistrictIdsDto.setLocalDistrictIds(geographyIds);

      response = electionsResultsRestClient.sumResultElectionsDistrictLocalIn(localDistrictIdsDto);
    } else if (roleName.equalsIgnoreCase("FEDERAL_DISTRICT_TRACKER")) {
      log.info("Federal District Tracker: ");
      List<UserFederalDistrictResponse> userFederalDistrictResponses =
          usersRestClient.readUserFederalDistrict();
      geographyIds =
          userFederalDistrictResponses.stream()
              .map(
                  federalDistrictResponse -> {
                    return Long.valueOf(federalDistrictResponse.getFederalDistrictId());
                  })
              .collect(Collectors.toList());
      federalDistrictIdsDto.setCandidatesIds(ids);
      federalDistrictIdsDto.setFederalDistrictIds(geographyIds);

      response =
          electionsResultsRestClient.sumResultElectionsDistrictFederalIn(federalDistrictIdsDto);
    }

    for (ElectionsAndCandidatesDto result : resultList) {
      int totalVotes = 0; // Variable para almacenar la suma de los resultados
      List<ResultDto> resultDtoList = new ArrayList<>();
      ElectionsAndCandidatesDto resultDto = new ElectionsAndCandidatesDto();
      for (PoliticalPartyResulResponse resp : response) {
        if (resp.getCandidateId().equals(result.getCandidateId())) {
          ResultDto electionResult = new ResultDto();
          electionResult.setCandidateId(resp.getCandidateId());
          electionResult.setPoliticalPartyName(resp.getPoliticalPartyName());
          electionResult.setResult(resp.getResult());

          // Buscar el softorder basado en el acronym
          PoliticalPartyEntity partyEntity = politicalPartyMap.get(resp.getPoliticalPartyName());
          if (partyEntity != null) {
            electionResult.setSoftorder(partyEntity.getSoftorder());
          }

          resultDtoList.add(electionResult);
          // Sumar el valor de result
          totalVotes += resp.getResult();
        }
      }
      resultDto.setCandidateId(result.getCandidateId());
      resultDto.setResultList(resultDtoList);
      resultDto.setTotalVotes(totalVotes);
      resultDto.setElectionPositionName(result.getElectionPositionName());
      resultDto.setCandidatePath(result.getCandidatePath());
      resultDto.setElectionName(result.getElectionName());
      resultDto.setNames(result.getNames());
      resultDto.setLastNames(result.getLastNames());
      resultDto.setSurNames(result.getSurNames());
      resultDto.setElectionId(result.getElectionId());

      dtoList.add(resultDto);
    }
    return dtoList;
  }

  @Override
  public List<ElectionVoterListDto> electionResultVoterList(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest) {
    return electionVoterListMapper.convertToDtoList(
        electionVoterListRepository.findByUserAuthId(authId));
  }

  @Override
  public List<ElectionVoterListSheetsDto> electionResultVoterListSheets(
      Integer electionsPositionsId, String authId, HttpServletRequest servletRequest) {
    List<ElectionVoterListSheetsDto> result = new ArrayList<>();

    List<Object[]> objects = electionRepository.getSheetsProgress();

    int idCounter = 1;
    for (Object[] objArray : objects) {

      ElectionVoterListSheetsDto dto = new ElectionVoterListSheetsDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setElectionId(objArray[0] != null ? Integer.parseInt(objArray[0].toString()) : 0);
      dto.setProgress(objArray[1] != null ? Integer.parseInt(objArray[1].toString()) : 1);
      dto.setTotal(objArray[2] != null ? Integer.parseInt(objArray[2].toString()) : 2);

      result.add(dto);
    }
    return result;
  }

  public String getRoleName(HttpServletRequest servletRequest) {
    Map<String, String> roleMap = new HashMap<>();
    roleMap.put("ADMIN", "ADMIN");
    roleMap.put("MUNICIPALITY_TRACKER", "MUNICIPALITY_TRACKER");
    roleMap.put("LOCAL_DISTRICT_TRACKER", "LOCAL_DISTRICT_TRACKER");
    roleMap.put("FEDERAL_DISTRICT_TRACKER", "FEDERAL_DISTRICT_TRACKER");

    for (Map.Entry<String, String> entry : roleMap.entrySet()) {
      if (servletRequest.isUserInRole(entry.getKey())) {
        return entry.getValue();
      }
    }
    return "Unknown Role"; // Devuelve un valor por defecto si no se encuentra ningún rol
  }
}
