package com.electorama.msvc.followers.model.mapper;

import com.electorama.msvc.followers.model.dto.FollowersDto;
import com.electorama.msvc.followers.model.entity.FollowersEntity;
import org.springframework.beans.BeanUtils;

public class FollowerMapper extends BaseMapper<FollowersEntity, FollowersDto> {

  private final CatSexMapper catSexMapper = new CatSexMapper();
  private final CatMaritalStatusMapper catMaritalStatusMapper = new CatMaritalStatusMapper();
  private final CatFamilyRoleMapper catFamilyRoleMapper = new CatFamilyRoleMapper();

  @Override
  public FollowersEntity convertToEntity(FollowersDto dto, Object... args) {
    FollowersEntity calendarEntity = new FollowersEntity();
    if (dto != null) {
      BeanUtils.copyProperties(dto, calendarEntity);
      if (dto.getCatSex() != null) {
        calendarEntity.setCatSex(catSexMapper.convertToEntity(dto.getCatSex()));
      }
      if (dto.getCatFamilyRole() != null) {
        calendarEntity.setCatFamilyRole(
            catFamilyRoleMapper.convertToEntity(dto.getCatFamilyRole()));
      }
      if (dto.getCatMaritalStatus() != null) {
        calendarEntity.setCatMaritalStatus(
            catMaritalStatusMapper.convertToEntity(dto.getCatMaritalStatus()));
      }
      if (dto.getId() != null) {
        calendarEntity.setId(dto.getId());
      }
    }
    return calendarEntity;
  }

  @Override
  public FollowersDto convertToDto(FollowersEntity entity, Object... args) {
    FollowersDto calendar = new FollowersDto();
    if (entity != null) {
      BeanUtils.copyProperties(entity, calendar);
      if (entity.getCatSex() != null)
        calendar.setCatSex(catSexMapper.convertToDto(entity.getCatSex()));
      calendar.setCatFamilyRole(catFamilyRoleMapper.convertToDto(entity.getCatFamilyRole()));
      calendar.setCatMaritalStatus(
          catMaritalStatusMapper.convertToDto(entity.getCatMaritalStatus()));
      if (entity.getId() != null) {
        calendar.setId(entity.getId());
      }
      calendar.setNames(entity.getNames());
      calendar.setLastName(entity.getLastName());
      calendar.setSurName(entity.getSurName());
      calendar.setCurp(entity.getCurp());
      calendar.setIne(entity.getIne());
      calendar.setBirthdate(entity.getBirthdate());
      calendar.setCellPhoneNumber(entity.getCellPhoneNumber());
      calendar.setCellPhoneNumberOther(entity.getCellPhoneNumberOther());
      calendar.setEmail(entity.getEmail());
      calendar.setBirthEntityId(entity.getBirthEntityId());
      calendar.setSectionNumber(entity.getSectionNumber());
      calendar.setInternalNumber(entity.getInternalNumber());
      calendar.setUserCreateId(entity.getUserCreateId());
      calendar.setDeleted(entity.getDeleted());
      calendar.setValidity(entity.getValidity());
      calendar.setIsAdult(entity.getIsAdult());
      calendar.setIsInSync(entity.getIsInSync());
      calendar.setStreet(entity.getStreet());
      calendar.setExternalNumber(entity.getExternalNumber());
      calendar.setInternalNumber(entity.getInternalNumber());
      calendar.setZipCode(entity.getZipCode());
      calendar.setResidenceEntityId(entity.getResidenceEntityId());
      calendar.setResidenceMunicipalityId(entity.getResidenceMunicipalityId());
      calendar.setSupportWhatsapp(entity.getSupportWhatsapp());
      calendar.setSupportsMovement(entity.getSupportsMovement());
      calendar.setCommitteeParticipation(entity.getCommitteeParticipation());
      calendar.setPollinStationRepresentativeParticipation(
          entity.getPollinStationRepresentativeParticipation());
      calendar.setInvitingOthersToParticipate(entity.getInvitingOthersToParticipate());
      calendar.setNumberOfPeopleInvited(entity.getNumberOfPeopleInvited());
      calendar.setLongitude(entity.getLongitude());
      calendar.setLatitude(entity.getLatitude());
      calendar.setNeighborhood(entity.getNeighborhood());
      calendar.setUuid(entity.getUuid());
      calendar.setDeviceId(entity.getDeviceId());
      calendar.setSectionNumberIne(entity.getSectionNumberIne());
    }
    return calendar;
  }
}
