package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionYearDto;
import com.electorama.elections.models.mapper.ElectionYearMapper;
import com.electorama.elections.models.repository.ElectionYearRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectionYearServiceImpl implements ElectionYearService {

  @Autowired ElectionYearRepository electionYearRepository;

  private final ElectionYearMapper electionYearMapper = new ElectionYearMapper();

  @Override
  public List<ElectionYearDto> getAllYears() {
    return new ArrayList<>(electionYearMapper.convertToDto(electionYearRepository.findAll()));
  }
}
