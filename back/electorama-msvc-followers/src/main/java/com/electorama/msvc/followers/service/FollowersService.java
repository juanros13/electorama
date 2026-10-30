package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.FollowersByPollingBoothsDto;
import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.dto.ResponseRelocateFollower;
import com.electorama.msvc.followers.model.dto.UpdateVoteFlagListDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface FollowersService {
  Page<FollowersDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      String promoterAuthId);

  FollowersDto save(FollowersDto followers, String authId);

  FollowersDto saveNew(FollowersDto followers, String authId);

  FollowersDto update(FollowersDto followers, String authId);

  Boolean relocateFollowers(ResponseRelocateFollower responseRelocateFollower);

  Optional<FollowersDto> byId(Long id, String authId);

  Optional<FollowersDto> referenceFindById(Long id, String authId);

  List<FollowersDto> byPromoterId(String id, String authId);

  List<FollowersDto> byCoordinatorId(String coordinatorAuthId, String authId);

  List<FollowersDto> byCoordinatorIdAlt(String coordinatorAuthId, String authId);

  void delete(Long id);

  FollowersDto findByCurp(String curp);

  FollowersDto findByIne(String ine);

  Page<FollowersDto> listByReferent(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      String coordinatorAuthId,
      String promoterAuthId);

  List<FollowersDto> listAll(String authId);

  List<FollowersByPollingBoothsDto> getFollowersByPollingBooths(List<Long> pollingBooths);

  FollowersDto updateVoteFlagById(Long id, boolean voteFlag);

  List<FollowersDto> updateVoteFlagList(UpdateVoteFlagListDto updateVoteFlagListDto);
}
