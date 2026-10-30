package com.electorama.geography.service;

import com.electorama.geography.model.dto.Neighborhoods;
import com.electorama.geography.model.dto.PostalCodeByMunicipality;
import com.electorama.geography.model.mapper.NeighborhoodsMapper;
import com.electorama.geography.model.mapper.PostalCodeByMunicipalityMapper;
import com.electorama.geography.model.repository.NeighborhoodsRepository;
import com.electorama.geography.model.repository.PostalCodeByMunicipalityRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostalCodeByMunicipalityServiceImpl implements PostalCodeByMunicipalityService {

  @Autowired PostalCodeByMunicipalityRepository postalCodeByMunicipalityRepository;

  @Autowired NeighborhoodsRepository neighborhoodsRepository;

  private final PostalCodeByMunicipalityMapper postalCodeByMunicipalityMapper =
      new PostalCodeByMunicipalityMapper();

  private final NeighborhoodsMapper neighborhoodsMapper = new NeighborhoodsMapper();

  @Override
  public List<PostalCodeByMunicipality> byMunicipalityId(Long municipalityId) {
    return (List<PostalCodeByMunicipality>)
        postalCodeByMunicipalityMapper.convertToDto(
            postalCodeByMunicipalityRepository.findByMunicipioId(municipalityId));
  }

  @Override
  public List<Neighborhoods> findNeighborhoodsByPostalCode(String postalCodeId) {
    return (List<Neighborhoods>)
        neighborhoodsMapper.convertToDto(neighborhoodsRepository.findByCpAsent(postalCodeId));
  }
}
