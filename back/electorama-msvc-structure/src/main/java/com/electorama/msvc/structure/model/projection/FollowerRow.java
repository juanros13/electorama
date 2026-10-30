package com.electorama.msvc.structure.model.projection;

import java.time.Instant;
import java.time.LocalDate;

/**
 * El convencido con su cadena de mando y su expediente de captura, en una fila.
 *
 * <p>Es lo que `msvc-followers` necesitaba del `followers ⋈ promoters` que hacía a mano contra la
 * base: el mismo dato, resuelto del lado del dueño. Por eso incluye los tres authId de la jerarquía
 * —promotor, coordinador de zona, referente— y, de paso, sus nombres, que el facade viejo dejaba
 * vacíos aunque el DTO los tuviera.
 */
public interface FollowerRow {

  Long getId();

  String getNames();

  String getLastName();

  String getSurName();

  String getCurp();

  String getIne();

  LocalDate getBirthdate();

  String getCellPhoneNumber();

  String getCellPhoneNumberOther();

  String getEmail();

  String getStatus();

  Instant getCreated();

  Instant getUpdated();

  Instant getDeleted();

  String getStreet();

  String getExternalNumber();

  String getInternalNumber();

  String getNeighborhood();

  String getZipCode();

  Integer getBirthEntityId();

  Integer getResidenceEntityId();

  Integer getResidenceMunicipalityId();

  // expediente de captura
  Long getCatSex();

  Long getCatMaritalStatus();

  Long getCatFamilyRole();

  Boolean getIsAdult();

  Boolean getIsInSync();

  Integer getValidity();

  String getSectionNumberIne();

  String getSupportWhatsapp();

  String getSupportsMovement();

  String getCommitteeParticipation();

  String getPollinStationRepresentativeParticipation();

  String getInvitingOthersToParticipate();

  String getNumberOfPeopleInvited();

  String getLatitude();

  String getLongitude();

  String getUuid();

  String getDeviceId();

  // linaje y jerarquía
  String getUserCreateId();

  String getAuthId();

  String getPromoterAuthId();

  String getPromoterAltAuthId();

  String getPromoterName();

  String getCoordinatorZoneAuthId();

  String getCoordinatorName();

  String getReferentAuthId();

  String getReferentName();

  // voto y territorio
  Boolean getVoteFlag();

  Instant getVoteFlagTimestamp();

  String getSectionNumber();

  Long getSectionId();

  Long getMunicipalityId();

  Long getFederalDistrictId();

  Long getLocalDistrictId();

  Long getPositionId();
}
