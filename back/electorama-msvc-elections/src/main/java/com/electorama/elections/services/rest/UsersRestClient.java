package com.electorama.elections.services.rest;

import com.electorama.elections.config.CustomFeignClientConfiguration;
import com.electorama.elections.models.rest.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

  @GetMapping("/v1.0.0/userMunicipalities")
  List<UserMunicipalityResponse> readUserAllCoorMunWithIdMun();

  @GetMapping("/v1.0.0/reportMetaReportByMunicipalities")
  List<UserResponse> readMetaMunicipalities();

  @GetMapping("/v1.0.0/reportMetaReportByCoordinateZone")
  List<UserCoordinateResponse> readMetaCoordinateZone();

  @GetMapping("/v1.0.0/readUser/{authId}")
  UserProfileResponse readUserXAuthId(@PathVariable("authId") String authId);

  @PostMapping("/v1.0.0/createPromoter")
  UserCreateResponse createPromoter(@RequestBody UserPromotersResponse user);

  @PutMapping("/v1.0.0/update/promotor/{authId}")
  UserCreateResponse updatePromotor(
      @PathVariable("authId") String authId, @RequestBody PromoterResponse user);

  @GetMapping("/v1.0.0/userSeccion/byRol/{authId}")
  List<UserSectionResponse> rolXSection(@PathVariable("authId") String authId);

  @DeleteMapping("/v1.0.0/deleteUser/{authId}")
  ResponseEntity deleteUserXAuthId(@PathVariable("authId") String authId);

  @GetMapping("/v1.0.0/findByCurp/{curp}")
  UserProfileResponse findByCurp(@PathVariable("curp") String curp);

  @GetMapping("/v1.0.0/findByIne/{ine}")
  UserProfileResponse findByIne(@PathVariable("ine") String ine);

  @GetMapping("/v1.0.0/userFederalDistrict")
  List<UserFederalDistrictResponse> readUserFederalDistrict();

  @GetMapping("/v1.0.0/userLocalDistrict")
  List<UserLocalDistrictResponse> readUserLocalDistrict();
}
