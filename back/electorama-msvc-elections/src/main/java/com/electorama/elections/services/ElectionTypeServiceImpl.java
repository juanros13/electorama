package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionTypeDto;
import com.electorama.elections.models.mapper.ElectionTypeMapper;
import com.electorama.elections.models.repository.ElectionTypeRepository;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElectionTypeServiceImpl implements ElectionTypeService {

  @Autowired ElectionTypeRepository electionTypeRepository;
  private final ElectionTypeMapper electionTypeMapper = new ElectionTypeMapper();

  @Override
  public List<ElectionTypeDto> getAllTypes() {
    return new ArrayList<>(electionTypeMapper.convertToDto(electionTypeRepository.findAll()));
  }
}
