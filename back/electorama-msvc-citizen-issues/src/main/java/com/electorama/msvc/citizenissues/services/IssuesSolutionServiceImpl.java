package com.electorama.msvc.citizenissues.services;

import com.electorama.msvc.citizenissues.model.dto.IssueSolutionDto;
import com.electorama.msvc.citizenissues.model.entity.IssueDetailEntity;
import com.electorama.msvc.citizenissues.model.entity.IssueSolutionEntity;
import com.electorama.msvc.citizenissues.model.mapper.IssueSolutionMapper;
import com.electorama.msvc.citizenissues.model.repository.IssueDetailRepository;
import com.electorama.msvc.citizenissues.model.repository.IssueSolutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class IssuesSolutionServiceImpl implements IssuesSolutionService {

  @Autowired private IssueSolutionRepository issueSolutionRepository;

  @Autowired private IssueDetailRepository issueDetailRepository;

  private final IssueSolutionMapper issueSolutionMapper = new IssueSolutionMapper();

  @Override
  public Page<IssueSolutionDto> findAll(
      Integer pageNo, Integer pageSize, String sortBy, String orderBy, String search) {

    Pageable pageable;

    if (orderBy.equals("desc")) {
      pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      pageable = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }

    return issueSolutionMapper.convertToDtoPage(issueSolutionRepository.findAll(pageable));
  }

  @Override
  public IssueSolutionDto findById(Long id) {
    return issueSolutionRepository.findById(id).map(issueSolutionMapper::convertToDto).orElse(null);
  }

  @Override
  public IssueSolutionDto save(IssueSolutionDto issueSolutionDto) {
    IssueDetailEntity entity =
        issueDetailRepository.findById(issueSolutionDto.getIssueDetailId()).orElse(null);

    IssueSolutionEntity issueSolutionEntitySave =
        issueSolutionMapper.convertToEntity(issueSolutionDto);
    IssueSolutionEntity issueSolutionEntity = issueSolutionRepository.save(issueSolutionEntitySave);
    entity.setIssueSolution(issueSolutionEntity);
    entity = issueDetailRepository.save(entity);
    IssueSolutionDto issueSolutionDto1 = issueSolutionMapper.convertToDto(issueSolutionEntity);
    issueSolutionDto1.setIssueDetailId(entity.getId());
    return issueSolutionDto1;
  }

  @Override
  public IssueSolutionDto update(IssueSolutionDto resourcesEntity) {
    return issueSolutionMapper.convertToDto(
        issueSolutionRepository.save(issueSolutionMapper.convertToEntity(resourcesEntity)));
  }

  @Override
  public IssueSolutionDto delete(Long id) {
    return issueSolutionMapper.convertToDto(
        issueSolutionRepository
            .findById(id)
            .map(
                issueSolutionEntity -> {
                  //  issueSolutionEntity.setDeleted(true);
                  return issueSolutionRepository.save(issueSolutionEntity);
                })
            .orElse(null));
  }
}
