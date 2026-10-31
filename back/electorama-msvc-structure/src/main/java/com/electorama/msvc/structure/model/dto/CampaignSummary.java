package com.electorama.msvc.structure.model.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Una campaña con sus estructuras, lo que se elige y sus niveles. El front toma de aquí los ids de
 * estructura (antes fijos en 1 y 2), los nombres de cada nivel (antes fijos en el front) y sabe si
 * la campaña es la activa o una cerrada, de sólo consulta.
 *
 * <p>{@code stateGeoId} / {@code municipalityGeoId} son los ids de geography de su territorio: con
 * ellos el front consulta el catálogo y el historial electoral del estado de la campaña.
 */
public record CampaignSummary(
    Long id,
    String name,
    String type,
    Integer year,
    LocalDate electionDay,
    String status,
    Long territoryId,
    String territoryName,
    Integer stateGeoId,
    Integer municipalityGeoId,
    BigDecimal goalPercentage,
    LocalDate startDate,
    LocalDate endDate,
    String description,
    List<StructureRef> structures,
    List<ElectionRef> elections,
    List<LevelRef> levels) {

  public record StructureRef(Long id, String name, String type) {}

  public record ElectionRef(String office, String scope, Integer seats, LocalDate electionDay) {}

  /**
   * Un nivel del organigrama. {@code positions} cuenta sus puestos: con alguno ya no cambian rango,
   * territorio ni función.
   */
  public record LevelRef(
      Long id,
      int rank,
      String code,
      String nameSingular,
      String namePlural,
      String territoryType,
      Integer defaultGoal,
      Integer defaultChildLimit,
      String function,
      String roleName,
      String alternateRoleName,
      long positions) {}
}
