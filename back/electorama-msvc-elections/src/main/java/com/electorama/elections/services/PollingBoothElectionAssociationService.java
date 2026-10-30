package com.electorama.elections.services;

import com.electorama.elections.models.dto.ElectionByPollingBoothsDto;
import com.electorama.elections.models.dto.PollingBoothElectionAssociationDto;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;

public interface PollingBoothElectionAssociationService {

  List<ElectionByPollingBoothsDto> getElectionsByPollingBooths(List<Long> pollingBooths);

  void updateSynchronized(Long id);

  PollingBoothElectionAssociationDto save(Long electionAssociationId, MultipartFile file);

  Optional<PollingBoothElectionAssociationDto> getPollingBoothsElectionAssociation(Long id);

  void updateValidate(Long id, Integer statusId);
}
