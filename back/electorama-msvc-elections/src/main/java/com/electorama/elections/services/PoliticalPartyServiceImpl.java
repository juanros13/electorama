package com.electorama.elections.services;

import com.electorama.elections.models.dto.PoliticalPartyDto;
import com.electorama.elections.models.entity.PoliticalPartyEntity;
import com.electorama.elections.models.mapper.PoliticanPartyMapper;
import com.electorama.elections.models.repository.PoliticalPartyRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PoliticalPartyServiceImpl implements PoliticalPartyService {
  @Autowired PoliticalPartyRepository politicalPartyRepository;

  private final PoliticanPartyMapper politicanPartyMapper = new PoliticanPartyMapper();

  @Override
  public List<PoliticalPartyDto> findAllPoliticalParties() {
    List<PoliticalPartyDto> politicalPartyDtos =
        politicanPartyMapper.convertToDtoList(politicalPartyRepository.findAll());
    return politicalPartyDtos;
  }

  @Override
  public List<PoliticalPartyDto> findByType(Long idType) {
    return politicanPartyMapper.convertToDtoList(
        politicalPartyRepository.findByPoliticalPartyType_Id(idType));
  }

  @Override
  public Optional<PoliticalPartyDto> findById(Long idPoliticalParty) {
    Optional<PoliticalPartyEntity> politicalPartyEntity =
        politicalPartyRepository.findById(idPoliticalParty);
    if (politicalPartyEntity.isPresent()) {
      return Optional.of(politicanPartyMapper.convertToDto(politicalPartyEntity.get()));
    }
    return Optional.empty();
  }
}
