package com.electorama.geography.controller;

import com.electorama.geography.model.dto.Neighborhoods;
import com.electorama.geography.model.dto.PostalCodeByMunicipality;
import com.electorama.geography.service.PostalCodeByMunicipalityService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
public class PostalCodeByMunicipalityController {

  @Autowired PostalCodeByMunicipalityService postalCodeByMunicipalityService;

  @GetMapping("/postalCodeByMunicipality/{municipalityId}")
  public ResponseEntity<?> detailPostalCodeByMunicipality(
      @PathVariable Long municipalityId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    List<PostalCodeByMunicipality> stateOptional =
        postalCodeByMunicipalityService.byMunicipalityId(municipalityId);
    if (!stateOptional.isEmpty()) {
      return ResponseEntity.ok(stateOptional);
    }
    return ResponseEntity.notFound().build();
  }

  @GetMapping("/detailNeighborhoodsByPostalCode/{postalCodeId}")
  public ResponseEntity<?> detailNeighborhoodsByPostalCode(
      @PathVariable String postalCodeId, @RequestHeader("X-Auth-Id") String authIdHeader) {
    List<Neighborhoods> stateOptional =
        postalCodeByMunicipalityService.findNeighborhoodsByPostalCode(postalCodeId);
    if (!stateOptional.isEmpty()) {
      return ResponseEntity.ok(stateOptional);
    }
    return ResponseEntity.notFound().build();
  }
}
