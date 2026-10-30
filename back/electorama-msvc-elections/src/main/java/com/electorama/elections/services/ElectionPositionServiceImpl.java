package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionPositionDto;
import com.electorama.elections.models.mapper.ElectionPositionMapper;
import com.electorama.elections.models.repository.ElectionPositionRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectionPositionServiceImpl implements ElectionPositionService {

  @Autowired ElectionPositionRepository electionPositionRepository;

  private final ElectionPositionMapper electionPositionMapper = new ElectionPositionMapper();

  @Override
  public List<ElectionPositionDto> getAllPositions() {
    return new ArrayList<>(
        electionPositionMapper.convertToDto(electionPositionRepository.findAll()));
  }
}
