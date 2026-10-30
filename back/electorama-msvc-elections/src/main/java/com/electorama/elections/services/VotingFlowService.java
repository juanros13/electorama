package com.electorama.elections.services;

import com.electorama.elections.models.dto.*;
import org.springframework.data.domain.Page;

public interface VotingFlowService {

  Page<VotingFlowDto> getVotingFlow(
      Integer pageNo,
      Integer pageSize,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId);

  ProgressVoteDto getProgressVote(
      Long municipalityId, Integer sectionNum, Long federalDistrictId, Long localDistrictId);
}
