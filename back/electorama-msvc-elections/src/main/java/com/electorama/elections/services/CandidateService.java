package com.electorama.elections.services;

import com.electorama.elections.models.dto.CandidateDto;
import java.util.List;

public interface CandidateService {

  List<CandidateDto> findAllCandidates();

  List<CandidateDto> findAllCandidatesByName(String name);

  List<CandidateDto> findByElection(Long idElection);
}
