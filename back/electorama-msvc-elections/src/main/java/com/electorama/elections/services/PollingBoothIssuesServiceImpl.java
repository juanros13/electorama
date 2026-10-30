package com.electorama.elections.services;

import com.electorama.elections.facade.PollingBoothIssuesFacade;
import com.electorama.elections.models.dto.*;
import com.electorama.elections.models.entity.PollingBoothEntity;
import com.electorama.elections.models.entity.PollingBoothIssuesEntity;
import com.electorama.elections.models.entity.PollingBoothStatusEntity;
import com.electorama.elections.models.mapper.PollingBoothIssuesCategoryMapper;
import com.electorama.elections.models.mapper.PollingBoothIssuesMapper;
import com.electorama.elections.models.mapper.PollingBoothIssuesSubCategoryMapper;
import com.electorama.elections.models.repository.PollingBoothIssuesCategoriesRepository;
import com.electorama.elections.models.repository.PollingBoothIssuesRepository;
import com.electorama.elections.models.repository.PollingBoothIssuesSubCategoryRepository;
import com.electorama.elections.models.repository.PollingBoothRepository;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PollingBoothIssuesServiceImpl implements PollingBoothIssuesService {

  @Autowired PollingBoothIssuesRepository pollingBoothIssuesRepository;
  @Autowired PollingBoothRepository pollingBoothRepository;
  @Autowired PollingBoothIssuesCategoriesRepository pollingBoothIssuesCategoriesRepository;
  @Autowired PollingBoothIssuesSubCategoryRepository pollingBoothIssuesSubCategoryRepository;

  @Autowired PollingBoothIssuesFacade pollingBoothIssuesFacade;
  private final PollingBoothIssuesMapper pollingBoothIssuesMapper = new PollingBoothIssuesMapper();
  private final PollingBoothIssuesCategoryMapper pollingBoothIssuesCategoryMapper =
      new PollingBoothIssuesCategoryMapper();

  private final PollingBoothIssuesSubCategoryMapper pollingBoothIssuesSubCategoryMapper =
      new PollingBoothIssuesSubCategoryMapper();

  @Override
  public List<PollingBoothIssuesDto> getIssuesByPollingBooth(Long pollingBoothId) {
    return new ArrayList<>(
        pollingBoothIssuesMapper.convertToDto(
            pollingBoothIssuesRepository.findByPollingBoothIdOrderByIdAsc(pollingBoothId)));
  }

  public PollingBoothIssuesDto addIssue(PollingBoothIssuesDto issue, String authId) {
    PollingBoothIssuesEntity issuesEntity = pollingBoothIssuesMapper.convertToEntity(issue, authId);

    Optional<PollingBoothEntity> entity =
        pollingBoothRepository.findById(issue.getPollingBoothId());

    if (entity.isPresent()) {
      PollingBoothEntity pollingBoothEntity = entity.get();
      PollingBoothStatusEntity status = new PollingBoothStatusEntity();

      if (pollingBoothEntity.getPollingBoothStatus().getId() == 1L) {
        status.setId(3L);
        pollingBoothEntity.setPollingBoothStatus(status);
      } else if (pollingBoothEntity.getPollingBoothStatus().getId() == 2L) {
        status.setId(4L);
        pollingBoothEntity.setPollingBoothStatus(status);
      }
      pollingBoothRepository.save(pollingBoothEntity);
    }

    issuesEntity.setIsSynchronized(true);
    PollingBoothIssuesDto issuesDto =
        pollingBoothIssuesMapper.convertToDto(pollingBoothIssuesRepository.save(issuesEntity));

    return issuesDto;
  }

  @Override
  public List<PollingBoothIssuesListDto> getIssuesByPollingBootList(List<Long> pollingBooths) {
    List<PollingBoothIssuesListDto> result = new ArrayList<>();
    AtomicLong idCounter = new AtomicLong(1);

    List<PollingBoothIssuesDto> pollingBoothIssuesDtos =
        pollingBoothIssuesMapper.convertToDtoList(
            pollingBoothIssuesRepository.findByPollingBoothIdIsIn(pollingBooths));

    for (Long booth : pollingBooths) {
      PollingBoothIssuesListDto dto = new PollingBoothIssuesListDto();
      List<PollingBoothIssuesDetailDto> detail = new ArrayList<>();
      for (PollingBoothIssuesDto pollingBoothIssuesDto : pollingBoothIssuesDtos) {
        if (booth.equals(pollingBoothIssuesDto.getPollingBoothId())) {
          PollingBoothIssuesDetailDto issuesDetailDto = new PollingBoothIssuesDetailDto();
          issuesDetailDto.setId(pollingBoothIssuesDto.getId());
          issuesDetailDto.setPollingBoothIssuesSubCategory(
              pollingBoothIssuesDto.getPollingBoothIssuesSubCategory());
          issuesDetailDto.setDescription(pollingBoothIssuesDto.getDescription());
          issuesDetailDto.setIsSynchronized(pollingBoothIssuesDto.getIsSynchronized());
          issuesDetailDto.setCreated(pollingBoothIssuesDto.getCreated());
          issuesDetailDto.setUpdated(pollingBoothIssuesDto.getUpdated());
          issuesDetailDto.setDeleted(pollingBoothIssuesDto.getDeleted());
          detail.add(issuesDetailDto);
        }
      }

      dto.setId(idCounter.getAndIncrement());
      dto.setPollingBoothId(booth);
      dto.setDetailsIssues(detail);
      result.add(dto);
    }
    return result;
  }

  @Override
  public List<PollingBoothIssuesCategoryDto> getCategory() {
    List<PollingBoothIssuesSubCategoryDto> sub = new ArrayList<>();
    List<PollingBoothIssuesCategoryDto> result = new ArrayList<>();

    result =
        pollingBoothIssuesCategoryMapper.convertToDtoList(
            pollingBoothIssuesCategoriesRepository.findAll());
    sub =
        pollingBoothIssuesSubCategoryMapper.convertToDtoList(
            pollingBoothIssuesSubCategoryRepository.findAll());

    for (PollingBoothIssuesCategoryDto category : result) {
      for (PollingBoothIssuesSubCategoryDto subCategory : sub) {
        if (subCategory.getPollingBoothIssuesCategory().getId() == category.getId()) {
          category.addSubCategory(subCategory);
        }
      }
    }

    return result;
  }

  @Override
  public Page<PollingBoothIssuesDescriptionDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<PollingBoothIssuesDescriptionDto> dtoPage = null;
    // if (!search.isEmpty() && !coordinatorId.isEmpty() && !promoterAuthId.isEmpty()) {
    // }
    dtoPage =
        pollingBoothIssuesFacade.findByParameters(
            municipalityId,
            sectionNum,
            federalDistrictId,
            localDistrictId,
            subCategoryId,
            typeId,
            paging);

    return dtoPage;
  }

  @Override
  public List<TotalIncidentsDto> listTotalIncidents(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId) {
    Pageable paging;

    List<TotalIncidentsDto> dtoPage = null;
    // if (!search.isEmpty() && !coordinatorId.isEmpty() && !promoterAuthId.isEmpty()) {
    // }
    dtoPage =
        pollingBoothIssuesFacade.findByTotalIncidents(
            municipalityId, sectionNum, federalDistrictId, localDistrictId, subCategoryId, typeId);

    return dtoPage;
  }

  @Override
  public List<PollingBoothIssuesDescriptionDto> downloadIssues(
      Long municipalityId,
      Integer sectionNum,
      Long federalDistrictId,
      Long localDistrictId,
      Long subCategoryId,
      Long typeId) {
    List<PollingBoothIssuesDescriptionDto> result = new ArrayList<>();

    List<PollingBoothIssuesDescriptionDto> objects =
        pollingBoothIssuesFacade.findByParametersList(
            municipalityId, sectionNum, federalDistrictId, localDistrictId, subCategoryId, typeId);

    int idCounter = 1;
    for (PollingBoothIssuesDescriptionDto obj : objects) {
      PollingBoothIssuesDescriptionDto edd = new PollingBoothIssuesDescriptionDto();
      edd.setId(Long.valueOf(idCounter++));
      edd.setMunicipality(obj.getMunicipality());
      edd.setFederalDistrict(obj.getFederalDistrict());
      edd.setLocalDistrict(obj.getLocalDistrict());
      edd.setSectionNumber(obj.getSectionNumber());
      edd.setPollingBoothsId(obj.getPollingBoothsId());
      edd.setNameCat(obj.getNameCat());
      edd.setNameSub(obj.getNameSub());
      edd.setDescription(obj.getDescription());
      edd.setCreated(obj.getCreated());
      edd.setPollingBoothName(obj.getPollingBoothName());

      result.add(edd);
    }
    return result;
  }
}
