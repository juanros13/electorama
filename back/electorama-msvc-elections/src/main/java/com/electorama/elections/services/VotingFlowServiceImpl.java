package com.electorama.elections.services;

import com.electorama.elections.facade.ReportMunicipalityFacade;
import com.electorama.elections.facade.ReportSectionsFacade;
import com.electorama.elections.facade.VotingFlowFacade;
import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.mapper.*;
import com.electorama.elections.models.repository.*;
import com.electorama.elections.models.rest.FollowerMarkProgressResponse;
import com.electorama.elections.services.rest.FollowersRestClient;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class VotingFlowServiceImpl implements VotingFlowService {

  @Autowired PollingBoothRepository pollingBoothRepository;

  @Autowired ReportMunicipalityFacade reportMunicipalityFacade;

  @Autowired ReportSectionsFacade reportSectionsFacade;

  @Autowired FollowersRestClient followersRestClient;
  @Autowired VotingFlowFacade votingFlowFacade;

  @Override
  public Page<VotingFlowDto> getVotingFlow(
      Integer pageNo,
      Integer pageSize,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long electionTypeId,
      Long electionPositionId) {
    Pageable paging;

    paging = PageRequest.of(pageNo, pageSize, Sort.by("id").descending());
    Page<VotingFlowDto> result = null;

    result =
        votingFlowFacade.getVotingFlow(
            municipalityId,
            sectionNum,
            federalDistrictId,
            localDistrictId,
            electionTypeId,
            electionPositionId,
            paging);

    List<Long> ids =
        result.stream().map(VotingFlowDto::getPollingBoothsId).collect(Collectors.toList());
    PollingBoothsIdsDto pollingBoothsIdsDto = new PollingBoothsIdsDto();
    pollingBoothsIdsDto.setPollingBoothsIds(ids);
    List<FollowerMarkProgressResponse> followerMarkProgressResponseList =
        followersRestClient.getFollowerMarkProgressIn(pollingBoothsIdsDto);

    Map<Long, Long> progressVoteMap =
        followerMarkProgressResponseList.stream()
            .collect(
                Collectors.toMap(
                    FollowerMarkProgressResponse::getPollingBoothId,
                    FollowerMarkProgressResponse::getProgressVote));

    for (VotingFlowDto votingFlowDto : result) {
      Long pollingBoothId = votingFlowDto.getPollingBoothsId();
      if (progressVoteMap.containsKey(pollingBoothId)) {
        votingFlowDto.setProgressVote(progressVoteMap.get(pollingBoothId));
      }
    }

    return result;
  }

  @Override
  public ProgressVoteDto getProgressVote(
      Long municipalityId, Integer sectionNum, Long federalDistrictId, Long localDistrictId) {

    VoterListDto result = null;

    result =
        votingFlowFacade.getProgressVote(
            municipalityId, sectionNum, federalDistrictId, localDistrictId);

    List<Long> pollingBoothIdList = convertStringToLongList(result.getPollingtBoothIds());

    PollingBoothsIdsDto pollingBoothsIdsDto = new PollingBoothsIdsDto();
    ProgressVoteDto progressVoteDto = new ProgressVoteDto();

    pollingBoothsIdsDto.setPollingBoothsIds(pollingBoothIdList);
    VoteDto voteDto = followersRestClient.getProgressVoteIn(pollingBoothsIdsDto);

    progressVoteDto.setProgressVote(voteDto.getProgressVote());
    progressVoteDto.setVoterList(result.getProgressVote());
    return progressVoteDto;
  }

  public List<Long> convertStringToLongList(String ids) {
    List<Long> idList = new ArrayList<>();

    if (ids != null && !ids.isEmpty()) {
      String[] idArray = ids.split(",");
      for (String id : idArray) {
        idList.add(Long.parseLong(id.trim()));
      }
    }

    return idList;
  }
}
