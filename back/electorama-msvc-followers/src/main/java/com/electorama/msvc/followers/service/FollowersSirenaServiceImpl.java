package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.model.dto.FollowersSirenaDto;
import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import com.electorama.msvc.followers.model.mapper.FollowersSirenaMapper;
import com.electorama.msvc.followers.model.repository.FollowersSirenaRepository;
import com.electorama.msvc.followers.model.rest.*;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowersSirenaServiceImpl implements FollowersSirenaService {

  @Autowired private FollowersSirenaRepository followersSirenaRepository;

  @Autowired private UsersRestClient usuariosRestClient;

  private final FollowersSirenaMapper followersSirenaMapper = new FollowersSirenaMapper();

  public Optional<FollowersSirenaDto> save(Optional<FollowersSirenaDto> followersSirenaDto) {

    FollowersSirenaEntity followersSirenaEntity =
        followersSirenaMapper.convertToEntity(followersSirenaDto.get());

    FollowersSirenaEntity followersSirenaSave =
        followersSirenaRepository.save(followersSirenaEntity);

    return Optional.ofNullable(followersSirenaMapper.convertToDto(followersSirenaSave));
  }

  /*********
   * @Override
   * public Optional<FollowersSirenaDto> byId(Long id, String authId) {
   * List<UserSectionResponse> userSectionResponses = usuariosRestClient.rolXSection(authId);
   * List<Long> listSections =
   * userSectionResponses.stream()
   * .map(userSectionResponse -> userSectionResponse.getSeccionGeoId())
   * .collect(Collectors.toList());
   *
   * List<FollowersSirenaDto> dtoList =
   * promotersMapper.convertToDtoList(
   * promotersSirenaRepository.findByCatCommittee_CatSectionIn(listSections));
   * Optional<FollowersSirenaDto> responseDto =
   * dtoList.stream()
   * .filter(
   * FollowersSirenaDto -> {
   * if (FollowersSirenaDto.getId().equals(id)) {
   * PromoterResponse userResponse = new PromoterResponse();
   *
   * userResponse.setEmail(FollowersSirenaDto.getEmail());
   * userResponse.setFirstName(FollowersSirenaDto.getNames());
   * userResponse.setLastName(
   * FollowersSirenaDto.getLastName() + " " + FollowersSirenaDto.getSurName());
   * FollowersSirenaDto.setUser(userResponse);
   * return true;
   * }
   * return false;
   * })
   * .findFirst();
   * return responseDto;
   *
   * //    return Optional.of(
   * //        promotersMapper.convertToDto(
   * //            promotersRepository
   * //                .findByIdAndUserCreateId(id, authId)
   * //                .orElseThrow(
   * //                    () -> new EntityNotFoundException("Usuario no encontrado con ID: " +
   * // id))));
   * }
   ***********/
  @Override
  public Optional<FollowersSirenaDto> byIdAdmin(Long id, String authId) {
    return Optional.of(
        followersSirenaMapper.convertToDto(
            followersSirenaRepository
                .findById(id)
                .orElseThrow(
                    () -> new EntityNotFoundException("Usuario no encontrado con ID: " + id))));
  }

  @Override
  public void delete(Long id) {
    followersSirenaRepository.deleteById(id);
  }

  @Override
  public Page<FollowersSirenaDto> list(
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

    //        if(search.length() >=1 ){
    //            entities = visitedRepository.findBySeccionId(search,
    //                    search,
    //                    search,
    //                    search,
    //                    search,
    //                    paging);
    //        }else{
    //            entities = visitedRepository.findByTramoId(paging);
    //        }

    List<UserSectionResponse> userSectionResponses = usuariosRestClient.rolXSection(authId);
    List<Long> listSections =
        userSectionResponses.stream()
            .map(
                userSectionResponse -> {
                  return userSectionResponse.getSeccionGeoId();
                })
            .collect(Collectors.toList());

    Page<FollowersSirenaDto> dtoPage =
        followersSirenaMapper.convertToDtoPage(
            followersSirenaRepository.findBySeccionIdIn(listSections, paging));

    //    List <CatCommitteeDto> listCommittee = new ArrayList<>();
    //
    //    userSectionResponses.stream().forEach(userSectionResponse -> {
    //      listCommittee.add(catCommitteeMaper.convertToDto(
    //              catCommitteeRepository
    //                      .findByCatSection(userSectionResponse.getSeccionGeoId())
    //                        .get(0))
    //                      );
    //    });
    /****
     * dtoPage.forEach(
     * promoter -> {
     * promoter
     * .getCatCommittee()
     * .setPromotersCount(
     * promotersSirenaRepository.countAllByCatCommitteeAndDeletedNull(
     * catCommitteeRepository.findById(promoter.getCatCommittee().getId()).get()));
     * });
     ***/
    return dtoPage;
  }

  @Override
  public Page<FollowersSirenaDto> listAdmin(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId) {
    Page<FollowersSirenaDto> dtoPage =
        followersSirenaMapper.convertToDtoPage(
            followersSirenaRepository.findAll(
                PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending())));
    /**********
     * dtoPage.forEach(
     * promoter -> {
     * promoter
     * .getCatCommittee()
     * .setPromotersCount(
     * promotersSirenaRepository.countAllByCatCommitteeAndDeletedNull(
     * catCommitteeRepository.findById(promoter.getCatCommittee().getId()).get()));
     * });
     *********/
    return dtoPage;
  }

  @Override
  public Optional<FollowersSirenaDto> byId(Long id, String authId) {
    return Optional.empty();
  }

  @Override
  public Page<FollowersSirenaDto> listBySection(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      Long sectionId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<FollowersSirenaDto> dtoPage = null;

    if (search.length() >= 1) {
      log.info("search...");

      dtoPage =
          followersSirenaMapper.convertToDtoPage(
              followersSirenaRepository.findByParametros(
                  sectionId, search, search, search, search, search, search, paging));

    } else {
      log.info("otro...");
      dtoPage =
          followersSirenaMapper.convertToDtoPage(
              followersSirenaRepository.findBySeccionIdAndNamesIsNotNull(sectionId, paging));
    }

    return dtoPage;
  }

  @Override
  public void deleteByFollowerId(Long id, String authId) {
    Optional<FollowersSirenaEntity> o = followersSirenaRepository.findByFollowerId(id);

    if (o.isPresent()) {
      FollowersSirenaEntity followersSirenaEntities = o.get();
      followersSirenaEntities.setFollowerId(null);
      followersSirenaRepository.save(followersSirenaEntities);
    }
  }
}
