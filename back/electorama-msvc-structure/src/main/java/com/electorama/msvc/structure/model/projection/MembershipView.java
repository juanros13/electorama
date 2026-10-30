package com.electorama.msvc.structure.model.projection;

import java.time.Instant;

/**
 * Un convencido como lo necesita la operación: la persona, su puesto actual y su linaje.
 *
 * <p>Los dos campos de linaje van juntos a propósito. {@code capturedByAssignmentId} responde qué
 * persona lo captó Y desde qué puesto lo hacía; {@code capturedByAuthId} guarda el dato crudo para
 * los 18,026 convencidos que capturaron cuentas que nunca ocuparon un puesto —administradores,
 * cargas masivas—, donde no hay asignación a la que apuntar.
 */
// Las vigencias llegan como Instant por lo mismo que en AssignmentHistory: el TSTZRANGE guarda un
// instante, no un desplazamiento.
public interface MembershipView {

  Long getId();

  Long getLegacyId();

  Long getPositionId();

  Long getStructureId();

  Long getPersonId();

  String getPersonName();

  String getCurp();

  String getIne();

  String getCellPhoneNumber();

  Integer getSectionGeoId();

  String getStatus();

  String getSource();

  Boolean getVoteFlag();

  Instant getValidFrom();

  Instant getValidTo();

  Long getCapturedByAssignmentId();

  String getCapturedByAuthId();

  /** Dónde se capturó (GPS del dispositivo o el punto que se marcó en el mapa). */
  Double getLatitude();

  Double getLongitude();
}
