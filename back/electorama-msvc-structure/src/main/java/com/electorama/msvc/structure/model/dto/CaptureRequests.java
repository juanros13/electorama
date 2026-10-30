package com.electorama.msvc.structure.model.dto;

import java.time.LocalDate;

/** La captura de convencidos desde el front: los mismos datos que pide la app. */
public final class CaptureRequests {

  private CaptureRequests() {}

  /**
   * Un convencido nuevo.
   *
   * @param positionId el puesto donde queda; sin él, el de quien captura
   * @param supportsMovement si simpatiza; sin simpatía sólo se guardan domicilio, sección y sexo
   * @param sex HOMBRE o MUJER (la CURP lo trae)
   * @param sectionGeoId la sección donde vive (catálogo de geography)
   * @param numberOfPeopleInvited uno, dos, tres, cuatro, cinco o masdecinco
   */
  public record Capture(
      Long positionId,
      Boolean supportsMovement,
      String names,
      String lastName,
      String surName,
      String curp,
      String ine,
      Integer ineValidity,
      LocalDate birthdate,
      String sex,
      String cellPhoneNumber,
      Boolean supportWhatsapp,
      String cellPhoneNumberOther,
      String email,
      Integer sectionGeoId,
      String street,
      String externalNumber,
      String internalNumber,
      String neighborhood,
      String zipCode,
      Boolean committeeParticipation,
      Boolean pollingRepParticipation,
      Boolean invitingOthers,
      String numberOfPeopleInvited,
      Double latitude,
      Double longitude) {}

  /** El convencido guardado: en qué puesto quedó y si la persona ya existía en el padrón. */
  public record Captured(
      Long membershipId, Long personId, Long positionId, String positionLabel, boolean newPerson) {}

  /** Un puesto donde quien captura puede dejar al convencido: el suyo o uno de su rama. */
  public record CaptureTarget(
      Long positionId,
      String level,
      String label,
      String holderName,
      Integer sectionGeoId,
      boolean own,
      Integer municipalityGeoId) {}
}
