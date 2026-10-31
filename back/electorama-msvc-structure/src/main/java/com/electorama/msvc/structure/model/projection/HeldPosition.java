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

  /** El rol de su nivel y, si lo tiene, el de la estructura alterna. */
  String getRoleName();

  String getAlternateRoleName();

  /** MAIN o ALTERNATE: decide el rol (_ALT). */
  String getStructureType();

  /** ACTIVE, PLANNING o CLOSED: lo de una campaña cerrada ya no pide rol. */
  String getCampaignStatus();

  /**
   * La raíz de su rama: el puesto con función REFERENT del que cuelga (el propio puesto si lo es)
   * o, si no hay, el de la cúspide.
   */
  Long getRootId();
}
