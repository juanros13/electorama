package com.electorama.geography.service.rest;

import com.electorama.geography.config.CustomFeignClientConfiguration;
import com.electorama.geography.model.rest.UserMunicipalityResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
    name = "${app.config.usuarios-url}",
    configuration = CustomFeignClientConfiguration.class,
    url = "${app.config.usuarios-url}")
public interface UsersRestClient {

  @GetMapping("/v1.0.0/userMunicipalities")
  List<UserMunicipalityResponse> readUserMunicipalities();
}
