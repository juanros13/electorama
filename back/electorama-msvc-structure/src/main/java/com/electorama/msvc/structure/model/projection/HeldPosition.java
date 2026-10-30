package com.electorama.msvc.structure.model.projection;

/**
 * Un puesto vigente que ocupa una cuenta, con el referente del que cuelga (su rama). Sirve para
 * saber, al ligar la cuenta a otro puesto, si ya estaba en esa misma rama.
 */
public interface HeldPosition {

  Long getPositionId();

  String getLevel();

  String getSectionKey();

  String getTerritoryCode();

  /** MAIN o ALTERNATE: decide el rol (_ALT). */
  String getStructureType();

  /** ACTIVE, PLANNING o CLOSED: lo de una campaña cerrada ya no pide rol. */
  String getCampaignStatus();

  /** El referente raíz de su rama; el propio puesto si es referente. */
  Long getRootId();
}
