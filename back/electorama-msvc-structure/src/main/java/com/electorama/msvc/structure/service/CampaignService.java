package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.CampaignSummary;
import com.electorama.msvc.structure.model.entity.Campaign;
import com.electorama.msvc.structure.model.enums.CampaignStatus;
import com.electorama.msvc.structure.model.projection.CampaignElectionRow;
import com.electorama.msvc.structure.model.repository.CampaignRepository;
import com.electorama.msvc.structure.model.repository.StructureRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Las campañas y cuál está en curso.
 *
 * <p>Todo lo operativo (organigrama, tablero, metas, captura, reportes) trabaja sobre UNA campaña:
 * la que se pida con {@code campaignId}, o la ACTIVE si no se pide. Así 2027 arranca en cero y 2024
 * se sigue leyendo completo para el análisis con {@code ?campaignId=} de la cerrada.
 */
@Service
@RequiredArgsConstructor
public class CampaignService {

  private final CampaignRepository campaigns;
  private final StructureRepository structures;

  @Transactional(readOnly = true)
  public List<CampaignSummary> all() {
    Map<Long, List<CampaignElectionRow>> elections =
        campaigns.elections().stream()
            .collect(Collectors.groupingBy(CampaignElectionRow::getCampaignId));
    return campaigns.findAllByOrderByYearDesc().stream()
        .map(c -> summary(c, elections.getOrDefault(c.getId(), List.of())))
        .toList();
  }

  @Transactional(readOnly = true)
  public CampaignSummary active() {
    Long id = activeId();
    return all().stream().filter(c -> c.id().equals(id)).findFirst().orElseThrow();
  }

  /** La campaña pedida si existe; si no se pide, la activa. */
  @Transactional(readOnly = true)
  public Long resolve(Long campaignId) {
    if (campaignId == null) {
      return activeId();
    }
    if (!campaigns.existsById(campaignId)) {
      throw StructureException.notFound("la campaña", campaignId);
    }
    return campaignId;
  }

  /** Si la campaña ya se cerró: V6 le congela la estructura y los convencidos. */
  @Transactional(readOnly = true)
  public boolean isClosed(Long campaignId) {
    return campaigns
        .findById(campaignId)
        .map(c -> c.getStatus() == CampaignStatus.CLOSED)
        .orElse(false);
  }

  private Long activeId() {
    return campaigns
        .findFirstByStatusOrderByIdDesc(CampaignStatus.ACTIVE)
        .map(Campaign::getId)
        .orElseThrow(
            () -> new StructureException(HttpStatus.CONFLICT, "no hay ninguna campaña activa"));
  }

  private CampaignSummary summary(Campaign c, List<CampaignElectionRow> elections) {
    return new CampaignSummary(
        c.getId(),
        c.getName(),
        c.getType().name(),
        c.getYear(),
        c.getElectionDay(),
        c.getStatus().name(),
        structures.findByCampaignIdOrderById(c.getId()).stream()
            .map(s -> new CampaignSummary.StructureRef(s.getId(), s.getName(), s.getType().name()))
            .toList(),
        elections.stream()
            .map(
                e ->
                    new CampaignSummary.ElectionRef(
                        e.getOffice(), e.getScope(), e.getSeats(), e.getElectionDay()))
            .toList());
  }
}
