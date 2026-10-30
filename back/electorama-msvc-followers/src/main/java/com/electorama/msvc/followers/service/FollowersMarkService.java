package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.*;
import java.util.List;

public interface FollowersMarkService {
  List<MarkByPollingBoothsDto> getFollowerMark(List<Long> pollingBooths);

  boolean updateVoteFlagAndSyncronizedList(
      UpdateVoteFlagAndSyncronizedListDto updateVoteFlagAndSyncronizedListDto);

  List<ReportFollowersMarkByPollingBoothDto> getReportFollowersMobilzationByPollingBooth(
      PollingBoothsIdsDto pollingBoothsIdsDto);

  List<FollowerMarkProgressDto> getProgressByFollowerMark(List<Long> pollingBooths);

  ProgressVoteDto getProgressVote(List<Long> pollingBooths);

  List<VotersParamsDto> getVotersParams(Integer id);
}
