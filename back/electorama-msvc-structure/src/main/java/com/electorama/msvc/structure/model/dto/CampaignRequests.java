package com.electorama.msvc.structure.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Lo que se manda para configurar una campaña (sólo ADMIN). */
public final class CampaignRequests {

  private CampaignRequests() {}

  /**
   * Los datos generales. {@code type}: FEDERAL, STATE, MUNICIPAL o INTERNAL. {@code
   * goalPercentage}: % de la LN que es meta en cada sección (61 si no se dice).
   */
  public record CampaignData(
      String name,
      String type,
      Integer year,
      LocalDate electionDay,
      Long territoryId,
      LocalDate startDate,
      LocalDate endDate,
      String description,
      BigDecimal goalPercentage) {}

  /** PLANNING → ACTIVE → CLOSED. */
  public record StatusChange(String status) {}

  /**
   * Un nivel; la lista se manda completa y en orden (el primero es la cúspide). {@code id} null es
   * un nivel nuevo. {@code roleName} null toma {@code <CAMPAÑA>_<CODIGO>}.
   */
  public record LevelData(
      Long id,
      String code,
      String nameSingular,
      String namePlural,
      String territoryType,
      Integer defaultGoal,
      Integer defaultChildLimit,
      String function,
      String roleName,
      String alternateRoleName) {}

  public record Levels(List<LevelData> levels) {}

  /** {@code office}: PRESIDENT, SENATOR, FEDERAL_DEPUTY, GOVERNOR, LOCAL_DEPUTY o MAYOR. */
  public record ElectionData(String office, String scope, Integer seats, LocalDate electionDay) {}

  public record Elections(List<ElectionData> elections) {}

  /** {@code type}: MAIN, ALTERNATE o ALLIED. */
  public record StructureData(String name, String type) {}
}
