package com.electorama.msvc.structure.model.projection;

import java.time.LocalDate;

/** Un cargo que se elige en una campaña: MAYOR ×17, LOCAL_DEPUTY ×21… */
public interface CampaignElectionRow {

  Long getCampaignId();

  /** Mismos códigos que msvc-electoral-history: MAYOR, LOCAL_DEPUTY, FEDERAL_DEPUTY… */
  String getOffice();

  /** MUNICIPALITY, LOCAL_DISTRICT, FEDERAL_DISTRICT o STATE. */
  String getScope();

  Integer getSeats();

  LocalDate getElectionDay();
}
