package com.electorama.msvc.followers.facade;

import com.electorama.msvc.followers.model.dto.CatFamilyRoleDto;
import com.electorama.msvc.followers.model.dto.CatMaritalStatusDto;
import com.electorama.msvc.followers.model.dto.CatSexDto;
import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.rest.StructureFollowerResponse;
import com.electorama.msvc.followers.model.rest.StructurePageResponse;
import com.electorama.msvc.followers.service.rest.StructureRestClient;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * Los convencidos de un coordinador de zona, con su cadena de mando.
 *
 * <p>Antes esto era un {@code followers ⋈ promoters} armado a mano contra la base local, con cuatro
 * problemas: el authId se concatenaba en el SQL, las excepciones se tragaban con un {@code
 * System.err.println} —así que un fallo devolvía una página vacía como si no hubiera convencidos—,
 * el conteo del total ignoraba el filtro por promotor y la misma consulta estaba copiada dos veces.
 *
 * <p>Ahora la jerarquía la resuelve msvc-structure, que es su dueño: el convencido cuelga de un
 * PUESTO y el puesto de otro puesto, así que la cadena promotor → coordinador → referente sale del
 * organigrama en vez de adivinarse cruzando authId. De paso llegan los tres nombres, que el DTO ya
 * tenía y esta consulta dejaba vacíos.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class FollowersPromotersFacade {

  private final StructureRestClient structure;

  public Page<FollowersDto> findByCordinatorAuthId(String authId, Pageable pageable) {
    return page(authId, null, pageable);
  }

  public Page<FollowersDto> findAllByPromoterAuthId(
      String authId, String promoterAuthId, Pageable pageable) {
    return page(authId, promoterAuthId, pageable);
  }

  private Page<FollowersDto> page(String authId, String promoterAuthId, Pageable pageable) {
    StructurePageResponse<StructureFollowerResponse> response =
        structure.findByCoordinator(
            authId, promoterAuthId, pageable.getPageNumber(), pageable.getPageSize());

    List<FollowersDto> content = response.getContent().stream().map(this::toDto).toList();
    return new PageImpl<>(content, pageable, response.getTotalElements());
  }

  private FollowersDto toDto(StructureFollowerResponse r) {
    FollowersDto dto = new FollowersDto();

    dto.setId(r.getId());
    dto.setNames(r.getNames());
    dto.setLastName(r.getLastName());
    dto.setSurName(r.getSurName());
    dto.setCurp(r.getCurp());
    dto.setIne(r.getIne());
    dto.setBirthdate(r.getBirthdate());
    dto.setCellPhoneNumber(r.getCellPhoneNumber());
    dto.setCellPhoneNumberOther(r.getCellPhoneNumberOther());
    dto.setEmail(r.getEmail());
    dto.setStatus(r.getStatus());
    dto.setCreated(r.getCreated());
    dto.setUpdated(r.getUpdated());
    dto.setDeleted(r.getDeleted());

    dto.setStreet(r.getStreet());
    dto.setExternalNumber(r.getExternalNumber());
    dto.setInternalNumber(r.getInternalNumber());
    dto.setNeighborhood(r.getNeighborhood());
    dto.setZipCode(r.getZipCode());
    dto.setBirthEntityId(r.getBirthEntityId());
    dto.setResidenceEntityId(r.getResidenceEntityId());
    dto.setResidenceMunicipalityId(r.getResidenceMunicipalityId());

    // Los catálogos viajan como id: viven en esta base, no en el modelo de
    // estructura, y el DTO los espera envueltos.
    dto.setCatSex(catSex(r.getCatSex()));
    dto.setCatMaritalStatus(catMaritalStatus(r.getCatMaritalStatus()));
    dto.setCatFamilyRole(catFamilyRole(r.getCatFamilyRole()));

    dto.setIsAdult(r.getIsAdult());
    dto.setIsInSync(r.getIsInSync());
    dto.setValidity(r.getValidity());
    dto.setSectionNumberIne(r.getSectionNumberIne());
    dto.setSupportWhatsapp(r.getSupportWhatsapp());
    dto.setSupportsMovement(r.getSupportsMovement());
    dto.setCommitteeParticipation(r.getCommitteeParticipation());
    dto.setPollinStationRepresentativeParticipation(
        r.getPollinStationRepresentativeParticipation());
    dto.setInvitingOthersToParticipate(r.getInvitingOthersToParticipate());
    dto.setNumberOfPeopleInvited(r.getNumberOfPeopleInvited());
    dto.setLatitude(r.getLatitude());
    dto.setLongitude(r.getLongitude());
    dto.setUuid(r.getUuid());
    dto.setDeviceId(r.getDeviceId());

    dto.setUserCreateId(r.getUserCreateId());
    dto.setAuthId(r.getAuthId());
    dto.setPromoterAuthId(r.getPromoterAuthId());
    dto.setPromoterAltAuthId(r.getPromoterAltAuthId());
    dto.setPromoterName(r.getPromoterName());
    dto.setCoordinatorZoneAuthId(r.getCoordinatorZoneAuthId());
    dto.setCoordinatorName(r.getCoordinatorName());
    dto.setReferentAuthId(r.getReferentAuthId());
    dto.setReferentName(r.getReferentName());

    dto.setVoteFlag(r.getVoteFlag());
    dto.setVoteFlagTimestamp(r.getVoteFlagTimestamp());
    dto.setSectionNumber(r.getSectionNumber());
    dto.setSectionId(r.getSectionId());
    dto.setMunicipalityId(r.getMunicipalityId());
    dto.setFederalDistrictId(r.getFederalDistrictId());
    dto.setLocalDistrictId(r.getLocalDistrictId());

    return dto;
  }

  private CatSexDto catSex(Long id) {
    if (id == null) {
      return null;
    }
    CatSexDto dto = new CatSexDto();
    dto.setId(id);
    return dto;
  }

  private CatMaritalStatusDto catMaritalStatus(Long id) {
    if (id == null) {
      return null;
    }
    CatMaritalStatusDto dto = new CatMaritalStatusDto();
    dto.setId(id);
    return dto;
  }

  private CatFamilyRoleDto catFamilyRole(Long id) {
    if (id == null) {
      return null;
    }
    CatFamilyRoleDto dto = new CatFamilyRoleDto();
    dto.setId(id);
    return dto;
  }
}
