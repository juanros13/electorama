package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.facade.ReportFollowersMobilizationByPollingBoothIdsFacade;
import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.model.entity.FollowerMarkEntity;
import com.electorama.msvc.followers.model.mapper.FollowerMarkMapper;
import com.electorama.msvc.followers.model.repository.*;
import com.electorama.msvc.followers.model.rest.PoliticalPartyResponse;
import com.electorama.msvc.followers.service.rest.ElectionsRestClient;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowersMarkServiceImpl implements FollowersMarkService {

  @Autowired private FollowersMarkRepository followersMarkRepository;

  @Autowired private FollowersRepository followersRepository;

  @PersistenceContext private EntityManager em;

  @Autowired
  private ReportFollowersMobilizationByPollingBoothIdsFacade
      reportFollowersMobilizationByPollingBoothIdsFacade;

  private final FollowerMarkMapper followerMarkMapper = new FollowerMarkMapper();

  @Autowired private ElectionsRestClient electionsRestClient;

  @Override
  public List<MarkByPollingBoothsDto> getFollowerMark(List<Long> pollingBooths) {
    List<MarkByPollingBoothsDto> result = new ArrayList<>();
    AtomicLong idCounter = new AtomicLong(1);

    List<FollowerMarkDto> markByPollingBoothsDtos =
        followerMarkMapper.convertToDtoList(
            followersMarkRepository.findByPollingBoothIdIsIn(pollingBooths));

    //    List<Long> idsFollowers =
    //        markByPollingBoothsDtos.stream()
    //            .map(FollowerMarkDto::getFollowerId)
    //            .collect(Collectors.toList());

    //    List<FollowersEntity> followersEntities =
    //        followersRepository.findByIdInAndPromoterAuthIdIsNotNull(idsFollowers);

    List<PoliticalPartyResponse> politicalPartyResponse = electionsRestClient.findAllElections();

    for (Long booth : pollingBooths) {
      MarkByPollingBoothsDto dto = new MarkByPollingBoothsDto();
      Map<Long, List<FollowersListDto>> followersMap = new HashMap<>();
      List<FollowerMarkIdsDto> markAll = new ArrayList<>();
      for (FollowerMarkDto markByPollingBooths : markByPollingBoothsDtos) {
        if (booth.equals(markByPollingBooths.getPollingBoothId())) {
          FollowerMarkIdsDto markIdsDto = new FollowerMarkIdsDto();
          markIdsDto.setId(markByPollingBooths.getId());

          //          for (FollowersEntity followersEntity : followersEntities) {
          //            if (followersEntity.getId().equals(markByPollingBooths.getFollowerId())) {
          //              FollowersListDto resDto = new FollowersListDto();
          //              resDto.setId(followersEntity.getId());
          //              resDto.setNames(followersEntity.getNames());
          //              resDto.setLastName(followersEntity.getLastName());
          //              resDto.setSurName(followersEntity.getSurName());
          //              resDto.setCurp(followersEntity.getCurp());
          //              resDto.setIne(followersEntity.getIne());
          //              resDto.setNeighborhood(followersEntity.getNeighborhood());
          //              resDto.setVoteFlag(followersEntity.getVoteFlag());
          //              resDto.setSectionNumber(followersEntity.getSectionNumber());
          //
          //              markIdsDto.setFollowers(resDto);
          //            }
          //          }
          FollowersListDto resDto = new FollowersListDto();
          resDto.setId(markByPollingBooths.getId());
          resDto.setNames("");
          resDto.setLastName("");
          resDto.setSurName("");
          resDto.setCurp("");
          resDto.setIne("");
          resDto.setNeighborhood("");
          resDto.setVoteFlag(markByPollingBooths.getMarkFlag());
          resDto.setSectionNumber("");
          resDto.setFullName(markByPollingBooths.getFullName());

          markIdsDto.setFollowers(resDto);
          //          for (PoliticalPartyResponse party : politicalPartyResponse) {
          //            if (party.getId() == markByPollingBooths.getPoliticalPartyId()) {
          //              markIdsDto.setPoliticalParty(party);
          //            }
          //          }

          markIdsDto.setMarkNumber(markByPollingBooths.getMarkNumber());
          markIdsDto.setMarkFlag(markByPollingBooths.getMarkFlag());
          markIdsDto.setIsSynchronized(markByPollingBooths.getIsSynchronized());
          markAll.add(markIdsDto);
        }
        dto.setMarkList(markAll);
      }
      dto.setId(idCounter.getAndIncrement());
      dto.setPollingBoothId(booth);
      result.add(dto);
    }
    return result;
  }

  public boolean updateIsSyncronizedById(Long id, boolean markFlag, boolean isSinchronized) {
    FollowerMarkEntity follower =
        followersMarkRepository
            .findById(id)
            .orElseThrow(
                () ->
                    new FollowersMarkServiceImpl.FollowerNotFoundException(
                        "Follower no encontrado con el ID: " + id));
    followersMarkRepository.updateIsSyncronizedById(id, markFlag, isSinchronized);
    return markFlag;
  }

  @Transactional
  public boolean updateVoteFlagAndSyncronizedList(UpdateVoteFlagAndSyncronizedListDto request) {
    boolean allUpdated = true;
    try {
      em.createNativeQuery(
              "UPDATE follower_mark  SET "
                  + " mark_flag = true, "
                  + " is_synchronized = true "
                  + " WHERE id in(?1) ")
          .setParameter(1, request.getIds())
          .executeUpdate();
    } catch (Exception e) {
      log.error("Error al actualizar la lista de seguidores", e);
    }

    //    for (Long id : request.getIds()) {
    //      boolean updated =
    //          updateIsSyncronizedById(id, request.getMarkFlag(), request.getIsSynchronized());
    //      if (!updated) {
    //        allUpdated = false;
    //      }
    //    }
    return allUpdated;
  }

  @Override
  public List<ReportFollowersMarkByPollingBoothDto> getReportFollowersMobilzationByPollingBooth(
      PollingBoothsIdsDto pollingBoothsIdsDto) {
    return reportFollowersMobilizationByPollingBoothIdsFacade.reportFollowersMobilization(
        pollingBoothsIdsDto.getPollingBoothsIds());
  }

  public class FollowerNotFoundException extends RuntimeException {
    public FollowerNotFoundException(String message) {
      super(message);
    }
  }

  @Override
  public List<FollowerMarkProgressDto> getProgressByFollowerMark(List<Long> pollingBoothsIds) {
    List<FollowerMarkProgressDto> result = new ArrayList<>();

    List<Object[]> objects = followersMarkRepository.getProgressByFollowerMark(pollingBoothsIds);

    int idCounter = 1;
    for (Object[] objArray : objects) {
      long pollingtBooth = ((Number) objArray[0]).longValue();
      long progress = ((Number) objArray[1]).longValue();
      FollowerMarkProgressDto dto = new FollowerMarkProgressDto();
      dto.setId(Long.valueOf(idCounter++));
      dto.setPollingBoothId(pollingtBooth);
      dto.setProgressVote(progress);

      result.add(dto);
    }
    return result;
  }

  @Override
  public ProgressVoteDto getProgressVote(List<Long> pollingBoothsIds) {
    ProgressVoteDto dto = new ProgressVoteDto();

    Object[] objects = followersMarkRepository.getProgressVote(pollingBoothsIds);

    long progress = objects[0] != null ? ((Number) objects[0]).longValue() : 0;

    dto.setProgressVote(progress);
    return dto;
  }

  @Override
  public List<VotersParamsDto> getVotersParams(Integer id) {
    List<VotersParamsDto> result = new ArrayList<>();
    List<Object[]> objects = null;

    // Municipality
    if (id == 1) {
      objects = followersMarkRepository.getVotersByMunicipality();

      for (Object[] objArray : objects) {
        VotersParamsDto dto = new VotersParamsDto();
        dto.setMunicipalityId(objArray[0] != null ? Integer.parseInt(objArray[0].toString()) : 0);
        dto.setMunicipality(objArray[1] != null ? objArray[1].toString() : "");
        dto.setNumVoters(objArray[2] != null ? Integer.parseInt(objArray[2].toString()) : 0);
        dto.setNumVotersPending(objArray[3] != null ? Integer.parseInt(objArray[3].toString()) : 0);
        result.add(dto);
      }

    } else if (id == 2) {
      objects = followersMarkRepository.getVotersByFederalDistrict();

      for (Object[] objArray : objects) {
        VotersParamsDto dto = new VotersParamsDto();
        dto.setFederalDistrict(objArray[0] != null ? Integer.parseInt(objArray[0].toString()) : 0);
        dto.setFederalDistrictName(objArray[1] != null ? objArray[1].toString() : "");
        dto.setNumVoters(objArray[2] != null ? Integer.parseInt(objArray[2].toString()) : 0);
        dto.setNumVotersPending(objArray[3] != null ? Integer.parseInt(objArray[3].toString()) : 0);
        result.add(dto);
      }
    } else if (id == 3) {
      objects = followersMarkRepository.getVotersByLocalDistrict();

      for (Object[] objArray : objects) {
        VotersParamsDto dto = new VotersParamsDto();
        dto.setLocalDistrict(objArray[0] != null ? Integer.parseInt(objArray[0].toString()) : 0);
        dto.setNumVoters(objArray[1] != null ? Integer.parseInt(objArray[1].toString()) : 0);
        dto.setNumVotersPending(objArray[2] != null ? Integer.parseInt(objArray[2].toString()) : 0);
        result.add(dto);
      }
    }

    return result;
  }
}
