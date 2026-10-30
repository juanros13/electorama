package com.electorama.geography.service;

import com.electorama.geography.model.dto.Neighborhoods;
import com.electorama.geography.model.dto.PostalCodeByMunicipality;
import java.util.List;

public interface PostalCodeByMunicipalityService {

  List<PostalCodeByMunicipality> byMunicipalityId(Long municipalityId);

  List<Neighborhoods> findNeighborhoodsByPostalCode(String postalCodeId);
}
