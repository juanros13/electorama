package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.dto.ResponseRelocateFollower;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface FollowersAltService {
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

  void delete(Long id);

  FollowersDto findByCurp(String curp);

  FollowersDto findByIne(String ine);

  Integer findTotalFollowerAlt(String authId);

  Page<FollowersDto> listByReferent(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);
}
