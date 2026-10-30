package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.FollowersSirenaDto;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface FollowersSirenaService {
  Page<FollowersSirenaDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

  Page<FollowersSirenaDto> listAdmin(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId);

  // FollowersSirenaDto save(FollowersSirenaDto promotersDto, String authId);

  Optional<FollowersSirenaDto> byId(Long id, String authId);

  Optional<FollowersSirenaDto> byIdAdmin(Long id, String authId);

  void deleteByFollowerId(Long id, String authId);

  void delete(Long id);

  Page<FollowersSirenaDto> listBySection(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      Long sectionId);

  Optional<FollowersSirenaDto> save(Optional<FollowersSirenaDto> followersSirenaDto);
}
