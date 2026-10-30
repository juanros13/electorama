package com.electorama.msvc.followers.service;

import com.electorama.msvc.followers.exeptions.DocumentManagerException;
import com.electorama.msvc.followers.exeptions.GlobalErrorCode;
import com.electorama.msvc.followers.model.dto.PromoterGoalsDto;
import com.electorama.msvc.followers.model.dto.PromotersDto;
import com.electorama.msvc.followers.model.entity.FollowersEntity;
import com.electorama.msvc.followers.model.entity.FollowersSirenaEntity;
import com.electorama.msvc.followers.model.entity.PromotersEntity;
import com.electorama.msvc.followers.model.mapper.CatCommitteeMapper;
import com.electorama.msvc.followers.model.mapper.PromotersMapper;
import com.electorama.msvc.followers.model.repository.FollowersRepository;
import com.electorama.msvc.followers.model.repository.FollowersSirenaRepository;
import com.electorama.msvc.followers.model.repository.PromotersRepository;
import com.electorama.msvc.followers.model.rest.*;
import com.electorama.msvc.followers.service.rest.UsersRestClient;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class PromotersServiceImpl implements PromotersService {

  @Autowired private PromotersRepository promotersRepository;

  @Autowired private FollowersRepository followersRepository;

  @Autowired private FollowersSirenaRepository followersSirenaRepository;

  @Autowired private UsersRestClient usuariosRestClient;

  @Value("${app.config.promotor-meta-follower}")
  private Integer promotorMetaFollower;

  @Value("${app.config.coordinator-meta-promotor}")
  private Integer coordinatorMetaPromotor;

  private CatCommitteeMapper catCommitteeMaper = new CatCommitteeMapper();

  private final PromotersMapper promotersMapper = new PromotersMapper();

  @Override
  public PromotersDto save(PromotersDto promotersDto, String authId) {
    FollowersEntity followerSearch = null;
    PromotersDto result = new PromotersDto();
    // Integer danyCount = promotersRepository.countAllByUserCreateIdAndDeletedNull(authId);

    String curp = promotersDto.getCurp();
    String ine = promotersDto.getIne();

    if (promotersDto.getId() == null && promotersRepository.existsByCurpAndFlagAlter(curp, false)) {
      throw new RuntimeException("Ya existe un promotor con la CURP proporcionada: " + curp);
    }

    // validamos si un coordinador se quiere dar de alta
    UserProfileResponse findByCurp = usuariosRestClient.findByCurp(curp);

    if (findByCurp.getId() != null) {
      throw new RuntimeException(
          "La CURP "
              + curp
              + " no se puede registrar como Promotor ya que esta dada de alta como Coordinador.");
    }

    // validamos si un COORDINADOR se quiere dar de alta por INE
    UserProfileResponse findCoordinatorByIne = usuariosRestClient.findByIne(ine);

    if (findCoordinatorByIne.getId() != null) {
      throw new RuntimeException(
          "La INE "
              + ine
              + " no se puede registrar como promotor ya que esta dada de alta como coordinador.");
    }

    // validamos si un PROMOTOR se quiere dar de alta por INE O CURP
    PromotersEntity findPromoterByCurpOrIne =
        promotersRepository.findFirstByFlagAlterIsFalseAndCurpOrIne(curp, ine);

    if (findPromoterByCurpOrIne != null && findPromoterByCurpOrIne.getId() != null) {
      throw new RuntimeException(
          "La CURP o INE "
              + curp
              + " - "
              + ine
              + " no se puede registrar como promotor ya que se encuentra registrado con otro promotor.");
    }

    followerSearch = followersRepository.findFirstByCurpOrIne(curp, ine);

    if (followerSearch != null && followerSearch.getPromoterAuthId() != null) {

      UserProfileResponse userProfileResponse =
          usuariosRestClient.readUserXAuthId(followerSearch.getPromoterAuthId());

      PromotersEntity promoters =
          promotersRepository.findFirstByPromoterAuthId(followerSearch.getPromoterAuthId());

      UserProfileResponse cordinatorProfileResponse =
          usuariosRestClient.readUserXAuthId(promoters.getCordinatorZoneAuthId());

      throw new RuntimeException(
          "La CURP y/o INE "
              + followerSearch.getCurp()
              + " "
              + followerSearch.getNames()
              + " "
              + followerSearch.getLastName()
              + " "
              + followerSearch.getSurName()
              + " ya esta registrada como simpatizante, con el promotor "
              + userProfileResponse.getFirstName()
              + " "
              + userProfileResponse.getLastName()
              + " que pertenece al Coordinador "
              + cordinatorProfileResponse.getFirstName()
              + " "
              + cordinatorProfileResponse.getLastName());
    }

    PromotersEntity promotersEntity = promotersMapper.convertToEntity(promotersDto, authId);

    if (promotersEntity.getFlagAlter() == null || !promotersEntity.getFlagAlter()) {
      promotersEntity.setFlagAlter(false);
    }

    Integer danyCount =
        promotersRepository.countAllByUserCreateIdAndSeccionIdAndFlagAlter(
            authId, promotersDto.getSeccionId(), false);
    if (promotersDto.getId() == null) {
      promotersEntity.setUserCreateId(authId);
      promotersEntity.setCordinatorZoneAuthId(authId);
    }

    if (promotersDto.getId() != null || danyCount <= coordinatorMetaPromotor) {
      UserPromotersResponse promotersResponse = new UserPromotersResponse();
      promotersResponse.setUser(promotersDto.getUser());
      if (promotersDto.getId() == null) {
        UserCreateResponse userResponse = usuariosRestClient.createPromoter(promotersResponse);
        promotersEntity.setPromoterAuthId(userResponse.getAuthId());

        UserProfileResponse userProfileResponse = usuariosRestClient.readUserXAuthId(authId);
        promotersEntity.setReferentAuthId(userProfileResponse.getAuthIdReference());
        promotersEntity.setFollowersGoal(promotorMetaFollower);
        promotersRepository.save(promotersEntity);
        result = promotersMapper.convertToDto(promotersRepository.save(promotersEntity));
        result.setPromoterAuthId(userResponse.getAuthId());
      }
      if (promotersDto.getId() != null) {
        result = promotersMapper.convertToDto(promotersRepository.save(promotersEntity));
        if (promotersDto.getUser() != null && promotersDto.getUser().getPassword() != null) {

          //          PromoterResponse userPromoter = new PromoterResponse();
          //          userPromoter.setEmail(promotersDto.getUser().getEmail());
          //          userPromoter.setPassword(promotersDto.getUser().getPassword());
          //          userPromoter.setFirstName(promotersDto.getUser().getFirstName());
          //          userPromoter.setLastName(promotersDto.getUser().getLastName());
          //
          //          usuariosRestClient.updatePromotor(promotersDto.getPromoterAuthId(),
          // userPromoter);
        }
      }
    } else {
      throw new DocumentManagerException(
          "La meta de 10 promotores por seccion ya fue alcanzada.",
          GlobalErrorCode.ERROR_PROMOTER_LIMIT);
    }
    return result;
  }

  @Override
  public Optional<PromotersDto> byId(Long id, String authId) {
    return Optional.of(
        promotersMapper.convertToDto(
            promotersRepository
                .findById(id)
                .orElseThrow(
                    () -> new EntityNotFoundException("Usuario no encontrado con ID: " + id))));
  }

  @Transactional
  public Optional<PromotersDto> deleteByPromoters(Long id, String authId) {

    Optional<PromotersDto> o =
        Optional.ofNullable(
            promotersMapper.convertToDto(
                promotersRepository
                    .findById(id)
                    .orElseThrow(
                        () -> new EntityNotFoundException("Usuario no encontrado con ID: " + id))));

    if (o.isPresent()) {
      Integer f =
          followersRepository.countAllByPromoterAuthIdAndPromoterAuthIdIsNotNull(
              o.get().getPromoterAuthId());

      if (f > 0) {
        throw new RuntimeException(
            "Este Promotor no se puede eliminar ya que cuenta con simpatizantes asignados: "
                + o.get().getNames()
                + " "
                + o.get().getLastName());
      } else {
        usuariosRestClient.deleteUserXAuthId(String.valueOf(o.get().getPromoterAuthId()));
        promotersRepository.deleteById(o.get().getId());
        Optional<FollowersSirenaEntity> fsList =
            Optional.ofNullable(followersSirenaRepository.findByPromotersId(o.get().getId()));

        if (fsList.isPresent()) {
          FollowersSirenaEntity followersSirenaEntities = fsList.get();
          followersSirenaEntities.setFollowerId(null);
          followersSirenaEntities.setPromotersId(null);
          followersSirenaRepository.save(followersSirenaEntities);
        }
      }
    }

    return o;
  }

  @Override
  public Optional<PromotersDto> byIdAdmin(Long id, String authId) {
    return Optional.of(
        promotersMapper.convertToDto(
            promotersRepository
                .findById(id)
                .orElseThrow(
                    () -> new EntityNotFoundException("Usuario no encontrado con ID: " + id))));
  }

  @Override
  public void delete(Long id) {
    promotersRepository.deleteById(id);
  }

  @Override
  public PromoterGoalsDto listBySection(Integer sectionId, String authId) {
    List<UserProfileResponse> userProfileResponse = usuariosRestClient.readUserAllKeycloak();
    PromoterGoalsDto promoterGoalsDto = new PromoterGoalsDto();
    Integer danyCount =
        promotersRepository.countAllByUserCreateIdAndSeccionIdAndFlagAlter(
            authId, sectionId, false);
    promoterGoalsDto.setPromotersCount(danyCount);
    promoterGoalsDto.setPromotersGoal(10);

    return promoterGoalsDto;
  }

  @Override
  public List<PromotersDto> listPromoterBySection(Integer sectionId, String authId) {
    List<PromotersDto> promotersDtos =
        promotersMapper.convertToDtoList(
            promotersRepository.findBySeccionIdAndCordinatorZoneAuthIdAndFlagAlter(
                sectionId, authId, false));

    promotersDtos.forEach(
        promoter -> {
          Integer followersCpunt =
              followersRepository.countAllByPromoterAuthIdAndPromoterAuthIdIsNotNull(
                  promoter.getPromoterAuthId());
          promoter.setFollowersCount(followersCpunt);
        });
    return promotersDtos;
  }

  @Override
  public List<PromotersDto> listPromoters(String authId) {
    List<PromotersDto> promotersDtos =
        promotersMapper.convertToDtoList(promotersRepository.findByCordinatorZoneAuthId(authId));
    promotersDtos.forEach(
        promoter -> {
          Integer followersCpunt =
              followersRepository.countAllByPromoterAuthIdAndPromoterAuthIdIsNotNull(
                  promoter.getPromoterAuthId());
          promoter.setFollowersCount(followersCpunt);
        });
    return promotersDtos;
  }

  @Override
  public Page<PromotersDto> list(
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

    //    List<UserSectionResponse> userSectionResponses = usuariosRestClient.rolXSection(authId);
    //    List<Long> listSections =
    //        userSectionResponses.stream()
    //            .map(
    //                userSectionResponse -> {
    //                  return userSectionResponse.getSeccionGeoId();
    //                })
    //            .collect(Collectors.toList());

    //    List <CatCommitteeDto> listCommittee = new ArrayList<>();
    //
    //    userSectionResponses.stream().forEach(userSectionResponse -> {
    //      listCommittee.add(catCommitteeMaper.convertToDto(
    //              catCommitteeRepository
    //                      .findByCatSection(userSectionResponse.getSeccionGeoId())
    //                        .get(0))
    //                      );
    //    });

    Page<PromotersDto> dtoPage =
        promotersMapper.convertToDtoPage(
            promotersRepository.findByCordinatorZoneAuthIdAndFlagAlter(
                authId, false, PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending())));
    dtoPage.forEach(
        promoter -> {
          Integer followersCpunt =
              followersRepository.countAllByPromoterAuthIdAndPromoterAuthIdIsNotNull(
                  promoter.getPromoterAuthId());
          promoter.setFollowersCount(followersCpunt);
        });
    //    dtoPage.forEach(
    //        promoter -> {
    //          promoter
    //              .getCatCommittee()
    //              .setPromotersCount(
    //                  promotersRepository.countAllByCatCommitteeAndDeletedNull(
    //
    // catCommitteeRepository.findById(promoter.getCatCommittee().getId()).get()));
    //        });

    return dtoPage;
  }

  @Override
  public Page<PromotersDto> listAdmin(
      Integer pageNo,
      Integer pageSize,
      String sortBy,
      String orderBy,
      String search,
      String authId) {
    Page<PromotersDto> dtoPage =
        promotersMapper.convertToDtoPage(
            promotersRepository.findAll(
                PageRequest.of(pageNo, pageSize, Sort.by(sortBy).descending())));

    //    dtoPage.forEach(
    //        promoter -> {
    //          promoter
    //              .getCatCommittee()
    //              .setPromotersCount(
    //                  promotersRepository.countAllByCatCommitteeAndDeletedNull(
    //
    // catCommitteeRepository.findById(promoter.getCatCommittee().getId()).get()));
    //        });

    return dtoPage;
  }

  public PromotersDto updatePromoterKeycloak(PromotersDto promotersDto, String authId) {
    PromotersDto result = new PromotersDto();

    PromoterResponse userPromoter = new PromoterResponse();
    // userPromoter.setEmail(promotersDto.getUser().getEmail());
    // userPromoter.setPassword(promotersDto.getUser().getPassword());
    userPromoter.setFirstName(promotersDto.getUser().getFirstName());
    userPromoter.setLastName(promotersDto.getUser().getLastName());

    usuariosRestClient.updatePromotor(promotersDto.getPromoterAuthId(), userPromoter);
    return result;
  }

  public PromotersDto update(PromotersDto promotersDto, Long id, String authIdHeader) {
    PromotersDto result = new PromotersDto();
    FollowersEntity followerSearch = null;

    Optional<PromotersDto> o = byIdAdmin(id, authIdHeader);

    if (o.isPresent()) {
      PromotersDto promotersDB = o.get();

      String curpActualizado = promotersDto.getCurp();
      String ineActualizado = promotersDto.getIne();

      if ((promotersDB.getId() != null && promotersDB.getCurp().equalsIgnoreCase(curpActualizado))
          || !promotersRepository.existsByCurpAndFlagAlter(curpActualizado, false)) {

        // validamos si un coordinador se quiere dar de alta
        UserProfileResponse findByCurp = usuariosRestClient.findByCurp(curpActualizado);

        if (findByCurp.getId() != null) {
          throw new RuntimeException(
              "La CURP "
                  + curpActualizado
                  + " no se puede registrar como Promotor ya que esta dada de alta como Coordinador.");
        }

        // validamos si un COORDINADOR se quiere dar de alta por INE
        UserProfileResponse findCoordinatorByIne = usuariosRestClient.findByIne(ineActualizado);

        if (findCoordinatorByIne.getId() != null) {
          throw new RuntimeException(
              "La INE "
                  + ineActualizado
                  + " no se puede registrar como promotor ya que esta dada de alta como coordinador.");
        }

        // validamos si un PROMOTOR se quiere dar de alta por INE O CURP
        PromotersEntity findPromoterByCurpOrIne =
            promotersRepository.findFirstByFlagAlterIsFalseAndCurpOrIne(
                curpActualizado, ineActualizado);

        if (findPromoterByCurpOrIne != null
            && findPromoterByCurpOrIne.getId() != null
            && findPromoterByCurpOrIne.getId() != id) {
          throw new RuntimeException(
              "La CURP o INE "
                  + curpActualizado
                  + " - "
                  + ineActualizado
                  + " no se puede registrar como promotor ya que se encuentra registrado con otro promotor.");
        }

        followerSearch = followersRepository.findFirstByCurpOrIne(curpActualizado, ineActualizado);

        if (followerSearch != null
            && followerSearch.getId() != null
            && followerSearch.getPromoterAuthId() != null) {

          UserProfileResponse userProfileResponse =
              usuariosRestClient.readUserXAuthId(followerSearch.getPromoterAuthId());

          PromotersEntity promoters =
              promotersRepository.findFirstByPromoterAuthId(followerSearch.getPromoterAuthId());

          UserProfileResponse cordinatorProfileResponse =
              usuariosRestClient.readUserXAuthId(promoters.getCordinatorZoneAuthId());

          throw new RuntimeException(
              "La CURP y/o INE "
                  + followerSearch.getCurp()
                  + " "
                  + followerSearch.getNames()
                  + " "
                  + followerSearch.getLastName()
                  + " "
                  + followerSearch.getSurName()
                  + " ya esta registrada como simpatizante, con el promotor "
                  + userProfileResponse.getFirstName()
                  + " "
                  + userProfileResponse.getLastName()
                  + " que pertenece al Coordinador "
                  + cordinatorProfileResponse.getFirstName()
                  + " "
                  + cordinatorProfileResponse.getLastName());
        }

        promotersDB.setNames(promotersDto.getNames());
        promotersDB.setSurName(promotersDto.getSurName());
        promotersDB.setLastName(promotersDto.getLastName());
        promotersDB.setEmail(promotersDto.getEmail());
        promotersDB.setCellPhoneNumber(promotersDto.getCellPhoneNumber());
        promotersDB.setCurp(promotersDto.getCurp());
        promotersDB.setIne(promotersDto.getIne());
        promotersDB.setInteriorNumber(promotersDto.getInteriorNumber());
        promotersDB.setNeighborhoodId(promotersDto.getNeighborhoodId());
        promotersDB.setNotes(promotersDto.getNotes());
        promotersDB.setOutdoorNumber(promotersDto.getOutdoorNumber());
        promotersDB.setPostalCode(promotersDto.getPostalCode());
        promotersDB.setStreet(promotersDto.getStreet());
        promotersDB.setVencimiento(promotersDto.getVencimiento());
        promotersDB.setPromoterAuthId(promotersDto.getPromoterAuthId());
        promotersDB.setUser(promotersDto.getUser());

        updatePromoterKeycloak(promotersDto, promotersDto.getPromoterAuthId());

        PromotersEntity promotersEntity =
            promotersMapper.convertToEntity(promotersDB, authIdHeader);

        if (promotersEntity.getFlagAlter() != true) promotersEntity.setFlagAlter(false);

        Integer danyCount =
            promotersRepository.countAllByUserCreateIdAndSeccionIdAndFlagAlter(
                authIdHeader, promotersDB.getSeccionId(), false);
        if (promotersDto.getId() == null) {
          promotersEntity.setUserCreateId(authIdHeader);
          promotersEntity.setCordinatorZoneAuthId(authIdHeader);
        }

        if (promotersDB.getId() != null || danyCount <= coordinatorMetaPromotor) {
          UserPromotersResponse promotersResponse = new UserPromotersResponse();
          promotersResponse.setUser(promotersDB.getUser());
          if (promotersDB.getId() == null) {
            UserCreateResponse userResponse = usuariosRestClient.createPromoter(promotersResponse);
            promotersEntity.setPromoterAuthId(userResponse.getAuthId());

            UserProfileResponse userProfileResponse =
                usuariosRestClient.readUserXAuthId(authIdHeader);
            promotersEntity.setReferentAuthId(userProfileResponse.getAuthIdReference());
            promotersEntity.setFollowersGoal(promotorMetaFollower);
            promotersRepository.save(promotersEntity);
            result = promotersMapper.convertToDto(promotersRepository.save(promotersEntity));
            result.setPromoterAuthId(userResponse.getAuthId());
          }
          if (promotersDB.getId() != null) {
            result = promotersMapper.convertToDto(promotersRepository.save(promotersEntity));
            if (promotersDB.getUser() != null && promotersDB.getUser().getPassword() != null) {

              //          PromoterResponse userPromoter = new PromoterResponse();
              //          userPromoter.setEmail(promotersDto.getUser().getEmail());
              //          userPromoter.setPassword(promotersDto.getUser().getPassword());
              //          userPromoter.setFirstName(promotersDto.getUser().getFirstName());
              //          userPromoter.setLastName(promotersDto.getUser().getLastName());
              //
              //          usuariosRestClient.updatePromotor(promotersDto.getPromoterAuthId(),
              // userPromoter);
            }
          }
        } else {
          throw new DocumentManagerException(
              "La meta de 10 promotores por seccion ya fue alcanzada.",
              GlobalErrorCode.ERROR_PROMOTER_LIMIT);
        }
      } else {
        throw new RuntimeException(
            "Ya existe un promotor con la CURP proporcionada: " + curpActualizado);
      }
    }
    return result;
  }

  public PromotersDto findByCurp(String curp) {
    return promotersMapper.convertToDto(promotersRepository.findByCurpAndFlagAlter(curp, false));
  }

  public PromotersDto findByIne(String ine) {
    return promotersMapper.convertToDto(promotersRepository.findByIneAndFlagAlter(ine, false));
  }
}
