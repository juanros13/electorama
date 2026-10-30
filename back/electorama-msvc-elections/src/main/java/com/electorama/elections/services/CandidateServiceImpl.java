package com.electorama.elections.services;

import com.electorama.elections.models.dto.CandidateDto;
import com.electorama.elections.models.mapper.CandidateMapper;
import com.electorama.elections.models.repository.CandidateRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandidateServiceImpl implements CandidateService {

  @Autowired CandidateRepository candidateRepository;

  private final CandidateMapper candidateMapper = new CandidateMapper();

  public List<CandidateDto> findAllCandidates() {
    return candidateMapper.convertToDtoList(candidateRepository.findAll());
  }

  @Override
  public List<CandidateDto> findAllCandidatesByName(String name) {
    return candidateMapper.convertToDtoList(candidateRepository.findAllByParametros(name));
  }

  @Override
  public List<CandidateDto> findByElection(Long idElection) {
    return candidateMapper.convertToDtoList(candidateRepository.findByElection_Id(idElection));
  }
}
