package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.facade.ReportCommitteesGoalFacade;
import com.electorama.msvc.followers.model.dto.CatCommitteeResponseDto;
import com.electorama.msvc.followers.model.dto.ReportCommitteesGoalsDto;
import com.electorama.msvc.followers.model.entity.CatCommitteeEntity;
import com.electorama.msvc.followers.model.mapper.CatCommitteeMapper;
import com.electorama.msvc.followers.model.repository.CatCommitteeRepository;
import com.electorama.msvc.followers.model.repository.PromotersRepository;
import com.electorama.msvc.followers.model.rest.SeccionResponse;
import com.electorama.msvc.followers.model.rest.UserSectionResponse;
import com.electorama.msvc.followers.service.rest.GeographyRestClient;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
public class CommitteesServiceImpl implements CommitteesService {

  @Autowired CatCommitteeRepository catCommitteeRepository;

  @Autowired PromotersRepository promotersRepository;

  @Autowired private UsersRestClient usuariosRestClient;

  @Autowired private ReportCommitteesGoalFacade reportCommitteesGoalFacade;

  @Autowired GeographyRestClient geographyRestClient;
  private CatCommitteeMapper catCommitteeMaper = new CatCommitteeMapper();

  @Override
  public Page<CatCommitteeResponseDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    List<UserSectionResponse> userSectionResponses = usuariosRestClient.rolXSection(authId);
    List<Long> listSections =
        userSectionResponses.stream()
            .map(
                userSectionResponse -> {
                  return userSectionResponse.getSeccionGeoId();
                })
            .collect(Collectors.toList());
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    // Page<CatCommitteeDto> dtoPage = catCommitteeMaper
    //        .convertToDtoPage(catCommitteeRepository.findByCatSectionIn(listSections, paging));

    Page<CatCommitteeEntity> entitiesPage =
        catCommitteeRepository.findByCatSectionIn(listSections, paging);

    List<CatCommitteeResponseDto> catCommitteeResponseDtoList =
        entitiesPage.stream()
            .map(
                committesEntity -> {
                  CatCommitteeResponseDto dto = new CatCommitteeResponseDto();
                  dto.setId(committesEntity.getId());
                  dto.setNoCommittee(committesEntity.getName());
                  dto.setGoal(committesEntity.getPromotersGoal());
                  dto.setElectoralBox(committesEntity.getElectoralBox());
                  if (committesEntity.getCatSection() != null) {
                    Optional<SeccionResponse> secc =
                        seccionResponse.stream()
                            .filter(
                                seccion -> {
                                  return committesEntity.getCatSection().equals(seccion.getId());
                                })
                            .findFirst();

                    Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
                    dto.setSectionNumber(seccionNumber != null ? seccionNumber.toString() : "");
                  } else {
                    dto.setSectionNumber("-SIN SECCION-");
                  }
                  //                  Integer progressCommittee =
                  //
                  // promotersRepository.countAllByCatCommitteeAndDeletedNull(committesEntity);
                  //                  dto.setProgress(progressCommittee);
                  return dto;
                })
            .collect(Collectors.toList());
    return new PageImpl<>(
        catCommitteeResponseDtoList, entitiesPage.getPageable(), entitiesPage.getTotalElements());
  }

  @Override
  public Page<CatCommitteeResponseDto> listAdmin(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId) {
    // Page<CatCommitteeDto> dtoPage = catCommitteeMaper
    //        .convertToDtoPage(catCommitteeRepository.findAll(PageRequest.of(pageNo, pageSize,
    // Sort.by(sortBy).descending())));
    Page<CatCommitteeEntity> entitiesPage =
        catCommitteeRepository.findAll(
            PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending()));
    List<SeccionResponse> seccionResponse = geographyRestClient.readSeccionByEntity();

    List<CatCommitteeResponseDto> catCommitteeResponseDtoList =
        entitiesPage.stream()
            .map(
                committesEntity -> {
                  CatCommitteeResponseDto dto = new CatCommitteeResponseDto();
                  dto.setId(committesEntity.getId());
                  dto.setNoCommittee(committesEntity.getName());
                  dto.setGoal(committesEntity.getPromotersGoal());
                  dto.setElectoralBox(committesEntity.getElectoralBox());
                  if (committesEntity.getCatSection() != null) {
                    Optional<SeccionResponse> secc =
                        seccionResponse.stream()
                            .filter(
                                seccion -> {
                                  return committesEntity.getCatSection().equals(seccion.getId());
                                })
                            .findFirst();

                    Integer seccionNumber = secc.map(SeccionResponse::getSeccion).orElse(null);
                    dto.setSectionNumber(seccionNumber != null ? seccionNumber.toString() : "");
                  } else {
                    dto.setSectionNumber("-SIN SECCION-");
                  }
                  //                  Integer progressCommittee =
                  //
                  // promotersRepository.countAllByCatCommitteeAndDeletedNull(committesEntity);
                  //                  dto.setProgress(progressCommittee);
                  return dto;
                })
            .collect(Collectors.toList());
    return new PageImpl<>(
        catCommitteeResponseDtoList, entitiesPage.getPageable(), entitiesPage.getTotalElements());
  }

  @Override
  public ReportCommitteesGoalsDto getReportCommitteesGoals(String authId) {
    List<UserSectionResponse> userSectionResponses = usuariosRestClient.rolXSection(authId);
    List<Integer> listSections =
        userSectionResponses.stream()
            .map(
                userSectionResponse -> {
                  return userSectionResponse.getSeccionGeoId().intValue();
                })
            .collect(Collectors.toList());
    return reportCommitteesGoalFacade.getReportCommitteesGoals(listSections);
  }

  @Override
  public ReportCommitteesGoalsDto getReportCommitteesGoalsByAdmin() {
    return reportCommitteesGoalFacade.getReportCommitteesGoalsByAdmin();
  }
}
