package com.electorama.msvc.structure.model.projection;

import java.time.Instant;

/**
 * Una asignación del historial de un puesto: quién lo ocupó, con qué cuenta y entre qué fechas.
 *
 * <p>Esto es lo que el modelo viejo no tenía: cero tablas de historial, cero Envers, cero triggers.
 * Y es lo que permite responder «quién era el coordinador de esta sección en marzo» sin adivinar.
 */
// `lower(validity)` y `upper(validity)` llegan del driver como Instant: un TSTZRANGE no lleva
// zona, lleva un punto en el tiempo. Declararlo OffsetDateTime hace que la proyección falle al
// serializar, así que el tipo dice la verdad del dato.
public interface AssignmentHistory {

  Long getId();

  Long getPersonId();

  String getAuthId();

  String getPersonName();

  String getRole();

  Instant getValidFrom();

  Instant getValidTo();

  String getStartReason();

  String getEndReason();
}
