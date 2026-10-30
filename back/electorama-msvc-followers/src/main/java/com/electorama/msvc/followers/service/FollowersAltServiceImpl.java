package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.facade.FollowersPromotersFacade;
import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.dto.FollowersSirenaDto;
import com.electorama.msvc.followers.model.dto.ResponseRelocateFollower;
import com.electorama.msvc.followers.model.entity.FollowersEntity;
import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import com.electorama.msvc.followers.model.entity.PromotersEntity;
import com.electorama.msvc.followers.model.mapper.FollowerMapper;
import com.electorama.msvc.followers.model.mapper.FollowersSirenaMapper;
import com.electorama.msvc.followers.model.repository.FollowersRepository;
import com.electorama.msvc.followers.model.repository.FollowersSirenaRepository;
import com.electorama.msvc.followers.model.repository.PromotersRepository;
import com.electorama.msvc.followers.model.rest.UserProfileResponse;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowersAltServiceImpl implements FollowersAltService {

  @Autowired private FollowersRepository followersRepository;

  @Autowired private PromotersRepository promotersRepository;
  @Autowired private FollowersSirenaRepository followersSirenaRepository;

  @Autowired private UsersRestClient usuariosRestClient;
  private final FollowersSirenaMapper followersSirenaMapper = new FollowersSirenaMapper();

  private final FollowerMapper followerMapper = new FollowerMapper();

  @Autowired FollowersPromotersFacade followersPromotersFacade;

  public FollowersDto save(FollowersDto followers, String authId) {
    FollowersEntity followerSearch = null;
    if (followers.getCurp() != null) {
      followerSearch = followersRepository.findFirstByCurp(followers.getCurp());
    } else if (followers.getUuid() != null) {
      followerSearch =
          followersRepository.findFirstByUuid(followers.getUuid()).orElse(new FollowersEntity());
    }
    if (followerSearch == null) {
      FollowersEntity followersEntity = followerMapper.convertToEntity(followers, authId);
      followersEntity.setUserCreateId(authId);
      followersEntity.setStatus("ACTIVO");
      FollowersDto followersDto =
          followerMapper.convertToDto(followersRepository.save(followersEntity));
      if (followers.getFollowerSirenaId() != null) {
        Optional<FollowersSirenaDto> followersSirenaDto =
            Optional.of(
                followersSirenaMapper.convertToDto(
                    followersSirenaRepository
                        .findById(followers.getFollowerSirenaId())
                        .orElseGet(null)));
        followersSirenaDto.get().setFollowerId(followersDto.getId());
        FollowersSirenaEntity followersSirenaEntity =
            followersSirenaMapper.convertToEntity(followersSirenaDto.get());
        followersSirenaRepository.save(followersSirenaEntity);
      }
      return followersDto;
    } else {
      FollowersDto followersDto = followerMapper.convertToDto(followerSearch);
      return followersDto;
    }
  }

  public FollowersDto saveNew(FollowersDto followers, String authId) {
    FollowersEntity followerSearch = null;

    if (followers.getCurp() != null && followers.getIne() != null) {

      followerSearch =
          followersRepository.findFirstByCurpOrIne(followers.getCurp(), followers.getIne());

      if (followerSearch == null) {
        FollowersEntity followersEntity = followerMapper.convertToEntity(followers, authId);
        followersEntity.setUserCreateId(authId);
        followersEntity.setStatus("ACTIVO");
        FollowersDto followersDto =
            followerMapper.convertToDto(followersRepository.save(followersEntity));
        return followersDto;
      } else if (followerSearch.getPromoterAltAuthId() != null) {
        UserProfileResponse userProfileResponse =
            usuariosRestClient.readUserXAuthId(followerSearch.getPromoterAltAuthId());

        PromotersEntity promoters =
            promotersRepository.findFirstByPromoterAuthId(followerSearch.getPromoterAltAuthId());

        UserProfileResponse cordinatorProfileResponse =
            usuariosRestClient.readUserXAuthId(promoters.getCordinatorZoneAuthId());

        throw new RuntimeException(
            "El simpatizante con CURP y/o INE "
                + followerSearch.getCurp()
                + " - "
                + followerSearch.getIne()
                + " "
                + followerSearch.getNames()
                + " "
                + followerSearch.getLastName()
                + " "
                + followerSearch.getSurName()
                + " ya esta asignado al promotor "
                + userProfileResponse.getFirstName()
                + " "
                + userProfileResponse.getLastName()
                + " y al coordinador "
                + cordinatorProfileResponse.getFirstName()
                + " "
                + cordinatorProfileResponse.getLastName());
      } else {
        throw new RuntimeException(
            "El simpatizante con CURP y/o INE " + " ya esta dado de alta en el sistema");
        //        followers.setId(followerSearch.getId());
        //        followerSearch.setPromoterAltAuthId(followers.getPromoterAltAuthId());
        //        followerSearch.setStatus("ACTIVO");
        //        FollowersDto followersDto =
        //            followerMapper.convertToDto(followersRepository.save(followerSearch));
        //        return followersDto;
      }
    }
    throw new RuntimeException("Los campos curp e ine son obligatorios");
  }

  public FollowersDto update(FollowersDto followers, String authId) {

    Optional<FollowersEntity> followerSearch = followersRepository.findById(followers.getId());
    if (followerSearch.isPresent()) {
      FollowersEntity followersEntity = followerMapper.convertToEntity(followers, authId);
      followersEntity.setStatus("ACTIVO");
      FollowersDto followersDto =
          followerMapper.convertToDto(followersRepository.save(followersEntity));
      return followersDto;
    }
    throw new RuntimeException("Usuario no encontrado");
  }

  @Override
  public Boolean relocateFollowers(ResponseRelocateFollower responseRelocateFollower) {
    try {
      for (int followerId : responseRelocateFollower.followersList) {
        Optional<FollowersEntity> followersEntity = followersRepository.findById((long) followerId);
        if (followersEntity.isPresent()) {
          FollowersEntity follower = followersEntity.get();
          follower.setPromoterAuthId(responseRelocateFollower.promoterAuthId);
          followersRepository.save(follower);
        }
      }
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  @Override
  public Optional<FollowersDto> byId(Long id, String authId) {
    return Optional.of(
        followerMapper.convertToDto(
            followersRepository
                .getByIdAndCoordinadorZoneAuthId(id, authId)
                .orElseThrow(
                    () -> new EntityNotFoundException("Follower no encontrado con ID: " + id))));
  }

  @Override
  public Optional<FollowersDto> referenceFindById(Long id, String authId) {
    return Optional.of(
        followerMapper.convertToDto(
            followersRepository
                .findById(id)
                .orElseThrow(
                    () -> new EntityNotFoundException("Follower no encontrado con ID: " + id))));
  }

  @Override
  public List<FollowersDto> byPromoterId(String promoterAuthId, String authId) {
    return followerMapper.convertToDtoList(
        followersRepository.findByPromoterAltAuthId(promoterAuthId));
  }

  @Override
  public void delete(Long id) {
    followersRepository.deleteById(id);
  }

  @Override
  public Page<FollowersDto> list(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      String promoterAuthId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<FollowersDto> dtoPage = null;

    if (search.length() >= 1) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findAllAltByParametros(search, search, authId, paging));
    } else if (!promoterAuthId.equals("")) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findAllAltByPromoterAuthId(authId, promoterAuthId, paging));
    } else {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findAltByCordinatorAuthId(authId, paging));
    }
    /***********
     * List<String> idsReferent = dtoPage.getContent().stream().map(FollowersDto::getReferentAuthId).collect(Collectors.toList());
     * List<String> idsCoordinator = dtoPage.getContent().stream().map(FollowersDto::getCoordinatorZoneAuthId).collect(Collectors.toList());
     *
     * HashSet<String> uniqueIdsReferent = new HashSet<>(idsReferent);
     * HashSet<String> uniqueIdsCoordinator = new HashSet<>(idsCoordinator);
     * List<String> uniqueIdsReferentList = new ArrayList<>(uniqueIdsReferent);
     * List<String> uniqueIdsCoordinatorList = new ArrayList<>(uniqueIdsCoordinator);
     * List<String> allUniqueIds = new ArrayList<>();
     *
     * allUniqueIds.addAll(uniqueIdsReferentList);
     * allUniqueIds.addAll(uniqueIdsCoordinatorList);
     *
     * List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(allUniqueIds);
     *******/
    dtoPage =
        new PageImpl<>(
            dtoPage.stream()
                .map(
                    page -> {
                      try {
                        UserProfileResponse userProfileResponse =
                            usuariosRestClient.readUserXAuthId(page.getPromoterAltAuthId());

                        page.setPromoterName(
                            userProfileResponse.getFirstName()
                                + " "
                                + userProfileResponse.getLastName());
                        /**********
                         * namesReferent.stream()
                         * .map(
                         * rep -> {
                         * if (page.getReferentAuthId().equalsIgnoreCase(rep.getAuthId())) {
                         * page.setReferentName(rep.getNombre());
                         * }else if (page.getCoordinatorZoneAuthId().equalsIgnoreCase(rep.getAuthId())){
                         * page.setCoordinatorName(rep.getNombre());
                         * }
                         * return rep;
                         * })
                         * .collect(Collectors.toList());
                         *********/
                      } catch (Exception e) {
                        page.setPromoterName("--");
                        log.error("Error al obtener el nombre del promotor", e);
                      }

                      return page;
                    })
                .collect(Collectors.toList()),
            dtoPage.getPageable(),
            dtoPage.getTotalElements());

    return dtoPage;
  }

  public FollowersDto findByCurp(String curp) {
    return followerMapper.convertToDto(followersRepository.findByCurp(curp));
  }

  public FollowersDto findByIne(String ine) {
    return followerMapper.convertToDto(followersRepository.findByIne(ine));
  }

  @Override
  public Integer findTotalFollowerAlt(String authId) {
    return followersRepository.findCountFollowersAlt(authId);
  }

  @Override
  public Page<FollowersDto> listByReferent(
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
    Page<FollowersDto> dtoPage = null;

    if (search.length() >= 1) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findByReferentParam(search, search, authId, paging));
    } else {
      dtoPage = followerMapper.convertToDtoPage(followersRepository.findByReferent(authId, paging));
    }

    //    List<String> idsReferent =
    // dtoPage.getContent().stream().map(FollowersDto::getReferentAuthId).collect(Collectors.toList());
    //    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

    dtoPage =
        new PageImpl<>(
            dtoPage.stream()
                .map(
                    page -> {
                      UserProfileResponse userProfileResponse =
                          usuariosRestClient.readUserXAuthId(page.getPromoterAuthId());
                      page.setPromoterName(
                          userProfileResponse.getFirstName()
                              + " "
                              + userProfileResponse.getLastName());
                      /***
                       * namesReferent.stream()
                       * .map(
                       * rep -> {
                       * if (page.getReferentAuthId().equalsIgnoreCase(rep.getAuthId())) {
                       * page.setReferentName(rep.getNombre());
                       * }else if (page.getCoordinatorZoneAuthId().equalsIgnoreCase(rep.getAuthId())){
                       * page.setCoordinatorName(rep.getNombre());
                       * }
                       * return rep;
                       * })
                       * .collect(Collectors.toList());
                       ****/
                      return page;
                    })
                .collect(Collectors.toList()),
            dtoPage.getPageable(),
            dtoPage.getTotalElements());

    return dtoPage;
  }
}
