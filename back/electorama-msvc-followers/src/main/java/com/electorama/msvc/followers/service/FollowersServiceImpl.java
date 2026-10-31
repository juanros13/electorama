package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.facade.FollowersPromotersFacade;
import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.model.entity.FollowersEntity;
import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import com.electorama.msvc.followers.model.entity.PromotersEntity;
import com.electorama.msvc.followers.model.mapper.FollowerMapper;
import com.electorama.msvc.followers.model.mapper.FollowerMarkMapper;
import com.electorama.msvc.followers.model.mapper.FollowersSirenaMapper;
import com.electorama.msvc.followers.model.repository.FollowerMarkRepository;
import com.electorama.msvc.followers.model.repository.FollowersRepository;
import com.electorama.msvc.followers.model.repository.FollowersSirenaRepository;
import com.electorama.msvc.followers.model.repository.PromotersRepository;
import com.electorama.msvc.followers.model.rest.UserProfileResponse;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowersServiceImpl implements FollowersService {

  @Autowired private FollowersRepository followersRepository;

  private final FollowersSirenaMapper followersSirenaMapper = new FollowersSirenaMapper();
  private final FollowerMapper followerMapper = new FollowerMapper();

  private final FollowerMarkMapper followerMarkMapper = new FollowerMarkMapper();

  public FollowersDto updateVoteFlagById(Long followerId, boolean newVoteFlag) {
    FollowersEntity follower =
        followersRepository
            .findById(followerId)
            .orElseThrow(
                () ->
                    new FollowerNotFoundException(
                        "Follower no encontrado con el ID: " + followerId));
    followersRepository.updateVoteFlagById(followerId, newVoteFlag);
    return followerMapper.convertToDto(follower);
  }

  public List<FollowersDto> updateVoteFlagList(UpdateVoteFlagListDto request) {
    List<FollowersDto> followers = new ArrayList<>();
    for (Long followerId : request.getFollowerIds()) {

      FollowersEntity follower =
          followersRepository
              .findById(followerId)
              .orElseThrow(
                  () ->
                      new FollowerNotFoundException(
                          "Follower no encontrado con el ID: " + followerId));
      updateVoteFlagById(followerId, request.getVoteFlag());
      followers.add(followerMapper.convertToDto(follower));
    }
    return followers;
  }

  public class FollowerNotFoundException extends RuntimeException {
    public FollowerNotFoundException(String message) {
      super(message);
    }
  }
}
