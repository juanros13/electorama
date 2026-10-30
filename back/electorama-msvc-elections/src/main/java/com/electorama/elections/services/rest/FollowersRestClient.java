package com.electorama.elections.services.rest;

import com.electorama.elections.config.CustomFeignClientConfiguration;
import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.rest.FollowerMarkProgressResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "${app.config.followers-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.followers-url}")
public interface FollowersRestClient {

  @PostMapping("/followersMark/getFollowerMarkProgress")
  List<FollowerMarkProgressResponse> getFollowerMarkProgressIn(
      @RequestBody PollingBoothsIdsDto pollingBoothsIdsDto);

  @PostMapping("/followersMark/getProgressVote")
  VoteDto getProgressVoteIn(@RequestBody PollingBoothsIdsDto pollingBoothsIdsDto);
}
