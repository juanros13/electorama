package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.facade.FollowersPromotersFacade;
import com.electorama.msvc.followers.model.dto.*;
import com.electorama.msvc.followers.model.entity.FollowersEntity;
import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import com.electorama.msvc.followers.model.entity.PromotersEntity;
import com.electorama.msvc.followers.model.mapper.FollowerMapper;
import com.electorama.msvc.followers.model.mapper.FollowerMarkMapper;
import com.electorama.msvc.followers.model.mapper.FollowersSirenaMapper;
import com.electorama.msvc.followers.model.repository.FollowerMarkRepository;
import com.electorama.msvc.followers.model.repository.FollowersRepository;
import com.electorama.msvc.followers.model.repository.FollowersSirenaRepository;
import com.electorama.msvc.followers.model.repository.PromotersRepository;
import com.electorama.msvc.followers.model.rest.UserProfileResponse;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FollowersServiceImpl implements FollowersService {

  @Autowired private FollowersRepository followersRepository;

  @Autowired private PromotersRepository promotersRepository;
  @Autowired private FollowersSirenaRepository followersSirenaRepository;

  @Autowired private FollowerMarkRepository followerMarkRepository;
  @Autowired private UsersRestClient usuariosRestClient;
  private final FollowersSirenaMapper followersSirenaMapper = new FollowersSirenaMapper();
  private final FollowerMapper followerMapper = new FollowerMapper();

  private final FollowerMarkMapper followerMarkMapper = new FollowerMarkMapper();

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
    FollowersEntity followerSearchAlt = null;

    if (followers.getCurp() != null && followers.getIne() != null) {

      followerSearchAlt =
          followersRepository.findFirstByPromoterAltAuthIdIsNotNullAndCurpOrIne(
              followers.getCurp(), followers.getIne());

      if (followerSearchAlt != null) {
        throw new RuntimeException(
            "El simpatizante con CURP y/o INE " + " ya esta dado de alta en el sistema");
      }

      // validamos si un COORDINADOR se quiere dar de alta por CURP
      UserProfileResponse findCoordinatorByCurp =
          usuariosRestClient.findByCurp(followers.getCurp());

      if (findCoordinatorByCurp.getId() != null) {
        throw new RuntimeException(
            "La CURP "
                + followers.getCurp()
                + " no se puede registrar como simpatizante ya que esta dada de alta como coordinador.");
      }

      // validamos si un COORDINADOR se quiere dar de alta por INE
      UserProfileResponse findCoordinatorByIne = usuariosRestClient.findByIne(followers.getIne());

      if (findCoordinatorByIne.getId() != null) {
        throw new RuntimeException(
            "La INE "
                + followers.getIne()
                + " no se puede registrar como simpatizante ya que esta dada de alta como coordinador.");
      }

      // validamos si un PROMOTOR se quiere dar de alta por INE O CURP
      PromotersEntity findPromoterByCurpOrIne =
          promotersRepository.findFirstByFlagAlterIsFalseAndCurpOrIne(
              followers.getCurp(), followers.getIne());

      if (findPromoterByCurpOrIne != null && findPromoterByCurpOrIne.getId() != null) {
        throw new RuntimeException(
            "La CURP o INE "
                + followers.getCurp()
                + " - "
                + followers.getIne()
                + " no se puede registrar como simpatizante ya que esta dada de alta como promotor.");
      }

      List<FollowersEntity> followerSearchAltCurp =
          followersRepository.findCurpAlt(followers.getCurp().substring(0, 16));

      if (!followerSearchAltCurp.isEmpty()) {
        throw new RuntimeException(
            "La CURP "
                + followers.getCurp()
                + " no se puede registrar como simpatizante ya que tiene homonimia con otro simpatizante.");
      }

      followerSearch =
          followersRepository.findFirstByCurpOrIne(followers.getCurp(), followers.getIne());

      if (followerSearch == null || followerSearch.getPromoterAuthId() == null) {
        if (followerSearch != null && followerSearch.getPromoterAuthId() == null) {
          followers.setId(followerSearch.getId());
        }
        if (followerSearch != null && followerSearch.getPromoterAltAuthId() != null) {
          followers.setPromoterAltAuthId(followerSearch.getPromoterAltAuthId());
        }
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

        UserProfileResponse userProfileResponse =
            usuariosRestClient.readUserXAuthId(followerSearch.getPromoterAuthId());

        PromotersEntity promoters =
            promotersRepository.findFirstByPromoterAuthId(followerSearch.getPromoterAuthId());

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
        followersRepository.findByPromoterAuthId(promoterAuthId));
  }

  @Override
  public List<FollowersDto> byCoordinatorId(String coordinatorAuthId, String authId) {
    List<FollowersDto> followersDtos =
        followerMapper.convertToDtoList(
            followersRepository.findAllByCoordinator(coordinatorAuthId));
    List<String> listIdsPromoters = new ArrayList<>();
    Set<String> uniqueIds = new HashSet<>();
    for (FollowersDto follower : followersDtos) {
      String id = follower.getPromoterAuthId();
      if (uniqueIds.add(id)) {
        listIdsPromoters.add(id);
      }
    }
    List<UserNamesListDto> usersPromoters = usuariosRestClient.getUsersList(listIdsPromoters);

    followersDtos =
        followersDtos.stream()
            .map(
                follower -> {
                  try {
                    for (UserNamesListDto user : usersPromoters) {
                      if (user.getAuthId().equals(follower.getPromoterAuthId())) {
                        follower.setPromoterName(user.getNombre());
                      }
                    }
                  } catch (Exception e) {
                    follower.setPromoterName("--");
                    log.error("Error al obtener el nombre del promotor", e);
                  }

                  return follower;
                })
            .collect(Collectors.toList());
    return followersDtos;
  }

  @Override
  public List<FollowersDto> byCoordinatorIdAlt(String coordinatorAuthId, String authId) {
    List<FollowersDto> followersDtos =
        followerMapper.convertToDtoList(
            followersRepository.findAllByCoordinatorAlt(coordinatorAuthId));
    List<String> listIdsPromoters = new ArrayList<>();
    Set<String> uniqueIds = new HashSet<>();
    for (FollowersDto follower : followersDtos) {
      String id = follower.getPromoterAuthId();
      if (uniqueIds.add(id)) {
        listIdsPromoters.add(id);
      }
    }
    List<UserNamesListDto> usersPromoters = usuariosRestClient.getUsersList(listIdsPromoters);

    followersDtos =
        followersDtos.stream()
            .map(
                follower -> {
                  try {
                    for (UserNamesListDto user : usersPromoters) {
                      if (user.getAuthId().equals(follower.getPromoterAuthId())) {
                        follower.setPromoterName(user.getNombre());
                      }
                    }
                  } catch (Exception e) {
                    follower.setPromoterName("--");
                    log.error("Error al obtener el nombre del promotor", e);
                  }

                  return follower;
                })
            .collect(Collectors.toList());
    return followersDtos;
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
              followersRepository.findAllByParametros(search, search, authId, paging));
    } else if (!promoterAuthId.equals("")) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findAllByPromoterAuthId(authId, promoterAuthId, paging));
    } else {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findByCordinatorAuthId(authId, paging));
    }
    //    List<String> idsReferent =
    //        dtoPage.getContent().stream()
    //            .map(FollowersDto::getReferentAuthId)
    //            .collect(Collectors.toList());
    //
    //    List<String> idsCoordinator =
    //        dtoPage.getContent().stream()
    //            .map(FollowersDto::getCoordinatorZoneAuthId)
    //            .collect(Collectors.toList());

    //    HashSet<String> uniqueIdsReferent = new HashSet<>(idsReferent);
    //    HashSet<String> uniqueIdsCoordinator = new HashSet<>(idsCoordinator);
    //    List<String> uniqueIdsReferentList = new ArrayList<>(uniqueIdsReferent);
    //    List<String> uniqueIdsCoordinatorList = new ArrayList<>(uniqueIdsCoordinator);
    //    List<String> allUniqueIds = new ArrayList<>();
    //
    //    allUniqueIds.addAll(uniqueIdsReferentList);
    //    allUniqueIds.addAll(uniqueIdsCoordinatorList);
    //
    //    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(allUniqueIds);
    dtoPage =
        new PageImpl<>(
            dtoPage.stream()
                .map(
                    page -> {
                      try {
                        UserProfileResponse userProfileResponse =
                            usuariosRestClient.readUserXAuthId(page.getPromoterAuthId());

                        page.setPromoterName(
                            userProfileResponse.getFirstName()
                                + " "
                                + userProfileResponse.getLastName());
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
  public Page<FollowersDto> listByReferent(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId,
      String coordinatorId,
      String promoterAuthId) {
    Pageable paging;

    if (orderBy.equals("desc")) {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending());
    } else {
      paging = PageRequest.of(pageNo, pageSize, Sort.by(sortBy).ascending());
    }
    Page<FollowersDto> dtoPage = null;
    if (!search.isEmpty() && !coordinatorId.isEmpty() && !promoterAuthId.isEmpty()) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findByReferentAndCoordinadtorAndPromotorParam(
                  search, search, authId, coordinatorId, promoterAuthId, paging));
    } else if (search.isEmpty() && !coordinatorId.isEmpty() && !promoterAuthId.isEmpty()) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findByReferentAndCoordinatorAndPromotor(
                  authId, coordinatorId, promoterAuthId, paging));
    } else if (!search.isEmpty() && !coordinatorId.isEmpty()) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findByReferentAndCoordinanatorParam(
                  search, search, authId, coordinatorId, paging));
    } else if (!coordinatorId.isEmpty()) {
      dtoPage =
          followerMapper.convertToDtoPage(
              followersRepository.findByReferentAndCoordinator(authId, coordinatorId, paging));
    } else {
      dtoPage = followerMapper.convertToDtoPage(followersRepository.findByReferent(authId, paging));
    }

    List<String> idsReferent =
        dtoPage.getContent().stream()
            .map(FollowersDto::getReferentAuthId)
            .collect(Collectors.toList());
    List<UserNamesListDto> namesReferent = usuariosRestClient.getUsersList(idsReferent);

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
                      return page;
                    })
                .collect(Collectors.toList()),
            dtoPage.getPageable(),
            dtoPage.getTotalElements());

    return dtoPage;
  }

  @Override
  public List<FollowersDto> listAll(String authId) {
    List<FollowersDto> followersDtos =
        followerMapper.convertToDtoList(followersRepository.findAllByCoordinator(authId));
    List<String> listIdsPromoters = new ArrayList<>();
    Set<String> uniqueIds = new HashSet<>();
    for (FollowersDto follower : followersDtos) {
      String id = follower.getPromoterAuthId();
      if (uniqueIds.add(id)) {
        listIdsPromoters.add(id);
      }
    }
    List<UserNamesListDto> usersPromoters = usuariosRestClient.getUsersList(listIdsPromoters);

    followersDtos =
        followersDtos.stream()
            .map(
                follower -> {
                  try {
                    for (UserNamesListDto user : usersPromoters) {
                      if (user.getAuthId().equals(follower.getPromoterAuthId())) {
                        follower.setPromoterName(user.getNombre());
                      }
                    }
                  } catch (Exception e) {
                    follower.setPromoterName("--");
                    log.error("Error al obtener el nombre del promotor", e);
                  }

                  return follower;
                })
            .collect(Collectors.toList());
    return followersDtos;
  }

  @Override
  public List<FollowersByPollingBoothsDto> getFollowersByPollingBooths(List<Long> pollingBooths) {
    List<FollowersByPollingBoothsDto> result = new ArrayList<>();
    AtomicLong idCounter = new AtomicLong(1);

    List<FollowerMarkDto> followerMarkDto =
        followerMarkMapper.convertToDtoList(
            followerMarkRepository.findByPollingBoothIdIn(pollingBooths));

    List<Long> idsFollowers =
        followerMarkDto.stream().map(FollowerMarkDto::getFollowerId).collect(Collectors.toList());

    List<FollowersEntity> followersEntities =
        followersRepository.findByIdInAndPromoterAuthIdIsNotNull(idsFollowers);

    Map<Long, List<FollowersListDto>> followersMap = new HashMap<>();
    for (FollowerMarkDto followerMark : followerMarkDto) {
      List<FollowersListDto> followersDtoList =
          followersMap.getOrDefault(followerMark.getPollingBoothId(), new ArrayList<>());
      for (FollowersEntity followersEntity : followersEntities) {
        if (followersEntity.getId().equals(followerMark.getFollowerId())) {
          FollowersListDto resDto = new FollowersListDto();
          resDto.setId(followersEntity.getId());
          resDto.setNames(followersEntity.getNames());
          resDto.setLastName(followersEntity.getLastName());
          resDto.setSurName(followersEntity.getSurName());
          resDto.setCurp(followersEntity.getCurp());
          resDto.setIne(followersEntity.getIne());
          resDto.setNeighborhood(followersEntity.getNeighborhood());
          resDto.setVoteFlag(followersEntity.getVoteFlag());
          resDto.setSectionNumber(followersEntity.getSectionNumber());
          resDto.setPoliticalPartyId(followerMark.getPoliticalPartyId());
          resDto.setMarkNumber(followerMark.getMarkNumber());

          followersDtoList.add(resDto);
        }
      }
      followersMap.put(followerMark.getPollingBoothId(), followersDtoList);
    }

    for (Map.Entry<Long, List<FollowersListDto>> entry : followersMap.entrySet()) {
      FollowersByPollingBoothsDto dto = new FollowersByPollingBoothsDto();
      dto.setId(idCounter.getAndIncrement());
      dto.setPollingBoothId(entry.getKey());
      dto.setFollowersList(entry.getValue());
      result.add(dto);
    }
    return result;
  }

  public FollowersDto updateVoteFlagById(Long followerId, boolean newVoteFlag) {
    FollowersEntity follower =
        followersRepository
            .findById(followerId)
            .orElseThrow(
                () ->
                    new FollowerNotFoundException(
                        "Follower no encontrado con el ID: " + followerId));
    followersRepository.updateVoteFlagById(followerId, newVoteFlag);
    return followerMapper.convertToDto(follower);
  }

  public List<FollowersDto> updateVoteFlagList(UpdateVoteFlagListDto request) {
    List<FollowersDto> followers = new ArrayList<>();
    for (Long followerId : request.getFollowerIds()) {

      FollowersEntity follower =
          followersRepository
              .findById(followerId)
              .orElseThrow(
                  () ->
                      new FollowerNotFoundException(
                          "Follower no encontrado con el ID: " + followerId));
      updateVoteFlagById(followerId, request.getVoteFlag());
      followers.add(followerMapper.convertToDto(follower));
    }
    return followers;
  }

  public class FollowerNotFoundException extends RuntimeException {
    public FollowerNotFoundException(String message) {
      super(message);
    }
  }
}
