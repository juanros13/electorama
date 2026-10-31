package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.FollowersByPollingBoothsDto;
import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.dto.ResponseRelocateFollower;
import com.electorama.msvc.followers.model.dto.UpdateVoteFlagListDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface FollowersService {
  FollowersDto updateVoteFlagById(Long id, boolean voteFlag);

  List<FollowersDto> updateVoteFlagList(UpdateVoteFlagListDto updateVoteFlagListDto);
}
