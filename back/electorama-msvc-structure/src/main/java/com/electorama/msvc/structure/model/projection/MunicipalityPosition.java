package com.electorama.msvc.structure.model.projection;

import java.time.Instant;

/**
 * Un puesto vigente de la estructura principal de un municipio, para el desglose del tablero de
 * campaña: referente, promotores (coordinadores de zona) y movilizadores (promotores) con su
 * titular y sus convencidos directos. El front arma el árbol con {@code parentId} y suma hacia
 * arriba.
 */
public interface MunicipalityPosition {

  Long getId();

  Long getParentId();

  /** REFERENT, ZONE_COORDINATOR o PROMOTER. */
  String getLevel();

  /** La sección del puesto; null en el referente. */
  Integer getSectionGeoId();

  /** "S-0233A1" o "S-0233A1-01"; null en el referente. */
  String getSectionKey();

  Integer getGoal();

  /** Nombre del titular vigente; null si el puesto está vacante. */
  String getHolderName();

  /** La persona titular: una puede ocupar varios puestos de promotor (varias secciones). */
  Long getHolderPersonId();

  /** Convencidos vigentes y activos anclados directamente al puesto. */
  Long getMembers();

  /** De esos, los capturados en los últimos 7 días. */
  Long getMembersLast7();

  /** La captura más reciente anclada al puesto; null si no tiene. */
  Instant getLastCapture();
}
