package com.electorama.geography.service;

import com.electorama.geography.exception.EntityNotFoundException;
import com.electorama.geography.model.dto.LocalDistrict;
import com.electorama.geography.model.mapper.LocalDistrictMapper;
import com.electorama.geography.model.repository.LocalDistrictRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocalDistrictServiceImpl implements LocalDistrictService {

  @Autowired LocalDistrictRepository localDistrictRepository;

  private final LocalDistrictMapper localDistrictMapper = new LocalDistrictMapper();

  @Override
  public Optional<LocalDistrict> byId(Long id) {
    return Optional.of(
        localDistrictMapper.convertToDto(
            localDistrictRepository.findById(id).orElseThrow(EntityNotFoundException::new)));
  }

  @Override
  public List<LocalDistrict> findByEntityId(Integer id) {
    return new ArrayList<>(
        localDistrictMapper.convertToDto(
            localDistrictRepository.findByEntidadIdOrderByDistritoLocalAsc(id)));
  }

  @Override
  public List<LocalDistrict> findByLocalDistricts() {
    return new ArrayList<>(localDistrictMapper.convertToDto(localDistrictRepository.findAll()));
  }
}
