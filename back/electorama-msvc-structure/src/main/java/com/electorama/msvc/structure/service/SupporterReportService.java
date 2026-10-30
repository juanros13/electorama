package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.model.projection.SupporterCoordinatorRow;
import com.electorama.msvc.structure.model.projection.SupporterProfileRow;
import com.electorama.msvc.structure.model.projection.SupporterSectionRow;
import com.electorama.msvc.structure.model.repository.SupporterReportRepository;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * El análisis de simpatizantes de una campaña. Sin {@code campaignId} es la activa; el front lo
 * pide con la cerrada (2024) para cruzarlo con el resultado oficial de esa elección.
 */
@Service
@RequiredArgsConstructor
public class SupporterReportService {

  private final SupporterReportRepository supporters;
  private final CampaignService campaigns;

  /**
   * El perfil de las campañas cerradas: recorre todos sus convencidos (3 s con los 1.07 M de 2024)
   * y no cambia, porque V6 congela membership. Sólo podría moverse el grupo de edad si se corrige
   * la fecha de nacimiento de una persona (person se comparte entre campañas); eso se ve al
   * reiniciar el servicio.
   */
  private final Map<Long, List<SupporterProfileRow>> closedProfiles = new ConcurrentHashMap<>();

  @Transactional(readOnly = true)
  public List<SupporterSectionRow> sections(Long campaignId) {
    return supporters.bySection(campaigns.resolve(campaignId));
  }

  @Transactional(readOnly = true)
  public List<SupporterCoordinatorRow> coordinators(Long campaignId) {
    return supporters.coordinators(campaigns.resolve(campaignId));
  }

  @Transactional(readOnly = true)
  public List<SupporterProfileRow> profile(Long campaignId) {
    Long id = campaigns.resolve(campaignId);
    if (!campaigns.isClosed(id)) {
      // si la reabrieron, lo guardado ya no vale
      closedProfiles.remove(id);
      return supporters.profile(id);
    }
    return closedProfiles.computeIfAbsent(id, k -> List.copyOf(supporters.profile(k)));
  }
}
