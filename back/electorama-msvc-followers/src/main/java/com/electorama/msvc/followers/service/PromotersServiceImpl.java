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

  @Autowired private UsersRestClient usuariosRestClient;

  @Value("${app.config.promotor-meta-follower}")
  private Integer promotorMetaFollower;

  @Value("${app.config.coordinator-meta-promotor}")
  private Integer coordinatorMetaPromotor;

  private CatCommitteeMapper catCommitteeMaper = new CatCommitteeMapper();

  private final PromotersMapper promotersMapper = new PromotersMapper();

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

}
