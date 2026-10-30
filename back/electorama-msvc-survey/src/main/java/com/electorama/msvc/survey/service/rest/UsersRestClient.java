package com.electorama.msvc.survey.service.rest;

import com.electorama.msvc.survey.config.CustomFeignClientConfiguration;
import com.electorama.msvc.survey.model.rest.UserProfileResponse;
import com.electorama.msvc.survey.model.rest.UserResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "${app.config.usuarios-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.usuarios-url}")
public interface UsersRestClient {

  @PostMapping("/v1.0.0/registerAlt")
  UserProfileResponse registerUser(@RequestBody UserProfileResponse user);

  @GetMapping("/v1.0.0/reportMetaReportByUser")
  List<UserResponse> readUsers();

  @GetMapping("/v1.0.0/profileExt")
  List<UserProfileResponse> readUserAllKeycloak();

  @GetMapping("/v1.0.0/reportMetaReportByMunicipalities")
  List<UserResponse> readMetaMunicipalities();

  @GetMapping("/v1.0.0/readUser/{authId}")
  UserProfileResponse readUserXAuthId(@PathVariable("authId") String authId);

  @DeleteMapping("/v1.0.0/deleteUser/{authId}")
  ResponseEntity deleteUserXAuthId(@PathVariable("authId") String authId);
}
