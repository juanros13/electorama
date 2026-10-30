package com.electorama.msvc.structure.model.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Una campaña con sus estructuras y lo que se elige. El front toma de aquí los ids de estructura
 * (antes fijos en 1 y 2) y sabe si la campaña es la activa o una cerrada, de sólo consulta.
 */
public record CampaignSummary(
    Long id,
    String name,
    String type,
    Integer year,
    LocalDate electionDay,
    String status,
    List<StructureRef> structures,
    List<ElectionRef> elections) {

  public record StructureRef(Long id, String name, String type) {}

  public record ElectionRef(String office, String scope, Integer seats, LocalDate electionDay) {}
}
