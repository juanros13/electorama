package com.electorama.geography.service;

import com.electorama.geography.exception.EntityNotFoundException;
import com.electorama.geography.model.dto.FederalDistrict;
import com.electorama.geography.model.mapper.FederalDistrictMapper;
import com.electorama.geography.model.repository.FederalDistrictRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FederalDistrictServiceImpl implements FederalDistrictService {

  @Autowired private FederalDistrictRepository federalDistrictRepository;

  private final FederalDistrictMapper federalDistrictMapper = new FederalDistrictMapper();

  @Override
  public Optional<FederalDistrict> byId(Long id) {
    return Optional.of(
        federalDistrictMapper.convertToDto(
            federalDistrictRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public List<FederalDistrict> findByEntityId(Integer id) {
    return new ArrayList<>(
        federalDistrictMapper.convertToDto(federalDistrictRepository.findByEntidadId(id)));
  }

  @Override
  public List<FederalDistrict> findByFederalDistricts() {
    return new ArrayList<>(federalDistrictMapper.convertToDto(federalDistrictRepository.findAll()));
  }
}
