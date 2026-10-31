package com.electorama.msvc.followers.service.rest;

import com.electorama.msvc.followers.config.CustomFeignClientConfiguration;
import com.electorama.msvc.followers.model.dto.UserNamesListDto;
import com.electorama.msvc.followers.model.rest.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "${app.config.usuarios-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.usuarios-url}")
public interface UsersRestClient {

  @PostMapping("/v1.0.0/getUsersList")
  List<UserNamesListDto> getUsersList(@RequestBody List<String> ids);
}
