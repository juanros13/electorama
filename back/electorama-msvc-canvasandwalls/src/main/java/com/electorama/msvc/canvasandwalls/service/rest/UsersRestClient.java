package com.electorama.msvc.canvasandwalls.service.rest;

import com.electorama.msvc.canvasandwalls.config.CustomFeignClientConfiguration;
import com.electorama.msvc.canvasandwalls.model.rest.UserCoordinateResponse;
import com.electorama.msvc.canvasandwalls.model.rest.UserMunicipalityResponse;
import com.electorama.msvc.canvasandwalls.model.rest.UserProfileResponse;
import com.electorama.msvc.canvasandwalls.model.rest.UserResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
    name = "${app.config.usuarios-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.usuarios-url}")
public interface UsersRestClient {

  @GetMapping("/v1.0.0/reportMetaReportByUser")
  List<UserResponse> readUsers();

  @GetMapping("/v1.0.0/profileExt")
  List<UserProfileResponse> readUserAllKeycloak();

  @GetMapping("/v1.0.0/profileExt/coorZone")
  List<UserProfileResponse> readUserAllCoorZone();

  @GetMapping("/v1.0.0/profileExt/coorMun")
  List<UserProfileResponse> readUserAllCoorMun();

  @GetMapping("/v1.0.0/profileExt/coorZoneAlt")
  List<UserProfileResponse> readUserAllCoorZoneAlt();

  @GetMapping("/v1.0.0/profileExt/coorMunAlt")
  List<UserProfileResponse> readUserAllCoorMunAlt();

  @GetMapping("/v1.0.0/userMunicipalities")
  List<UserMunicipalityResponse> readUserAllCoorMunWithIdMun();

  @GetMapping("/v1.0.0/reportMetaReportByMunicipalities")
  List<UserResponse> readMetaMunicipalities();

  @GetMapping("/v1.0.0/reportMetaReportByCoordinateZone")
  List<UserCoordinateResponse> readMetaCoordinateZone();

  @GetMapping("/v1.0.0/readUser/{authId}")
  UserProfileResponse readUserXAuthId(@PathVariable("authId") String authId);
}
