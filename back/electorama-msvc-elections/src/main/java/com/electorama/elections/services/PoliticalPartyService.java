package com.electorama.elections.services;

import com.electorama.elections.models.dto.PoliticalPartyDto;
import java.util.List;
import java.util.Optional;

public interface PoliticalPartyService {

  List<PoliticalPartyDto> findAllPoliticalParties();

  List<PoliticalPartyDto> findByType(Long idType);

  Optional<PoliticalPartyDto> findById(Long idPoliticalParty);
}
