package com.electorama.msvc.followers.controller;

import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.dto.UpdateVoteFlagListDto;
import com.electorama.msvc.followers.service.FollowersService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Lo que queda de la API de seguidores del flujo 2024: la marca de voto del día de la elección. La
 * captura de convencidos vive en msvc-structure ({@code POST /memberships}).
 */
@Slf4j
@RestController
@RequestMapping(value = "/followers")
public class FollowersController {

  @Autowired private FollowersService followersService;

  @PutMapping("/updateVoteFlag/{followerId}")
  public ResponseEntity<?> updateVoteFlag(
      @PathVariable Long followerId,
      @RequestParam boolean voteFlag,
      @RequestHeader("X-Auth-Id") String authIdHeader) {

    FollowersDto followersDto = followersService.updateVoteFlagById(followerId, voteFlag);

    return ResponseEntity.ok(followersDto);
  }

  @PostMapping("/updateVoteFlagList")
  public ResponseEntity<?> updateVoteFlagList(
      @RequestBody UpdateVoteFlagListDto request, @RequestHeader("X-Auth-Id") String authIdHeader) {

    List<FollowersDto> followersDtoList = followersService.updateVoteFlagList(request);
    return ResponseEntity.ok(followersDtoList);
  }
}
