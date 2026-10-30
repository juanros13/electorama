package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.projection.FollowerCountRow;
import com.electorama.msvc.structure.model.repository.ReportQueryRepository;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Los reportes de captación, agrupados por el nivel que pida quien consulta. */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

  /** Cómo se agrupa el reporte. Es lo que el modelo viejo tenía como tres facades distintos. */
  public enum GroupBy {
    MUNICIPALITY,
    COORDINATOR,
    REFERENT
  }

  private final ReportQueryRepository reports;
  private final CampaignService campaigns;

  @Transactional(readOnly = true)
  public List<FollowerCountRow> followers(
      GroupBy groupBy,
      OffsetDateTime from,
      OffsetDateTime to,
      String scopeAuthId,
      Long campaignId) {
    if (from == null || to == null) {
      throw StructureException.badRequest("el reporte necesita rango de fechas (from y to)");
    }
    if (!to.isAfter(from)) {
      throw StructureException.badRequest("el rango de fechas está invertido");
    }
    boolean scoped = scopeAuthId != null && !scopeAuthId.isBlank();
    Long campaign = campaigns.resolve(campaignId);

    return switch (groupBy) {
      case MUNICIPALITY -> scoped
          ? reports.byMunicipalityScoped(from, to, scopeAuthId, campaign)
          : reports.byMunicipality(from, to, campaign);
      case COORDINATOR -> scoped
          ? reports.byCoordinatorScoped(from, to, scopeAuthId, campaign)
          : reports.byCoordinator(from, to, campaign);
      case REFERENT -> scoped
          ? reports.byReferentScoped(from, to, scopeAuthId, campaign)
          : reports.byReferent(from, to, campaign);
    };
  }

  /**
   * Movilización de una sección: con marca de voto contra el total.
   *
   * <p>Se puede pedir por id del catálogo o por número impreso. El reporte viejo usaba el número,
   * así que hay que aceptar los dos mientras el front no se toque.
   */
  @Transactional(readOnly = true)
  public Map<String, Long> mobilization(
      Integer sectionGeoId, Integer sectionNumber, Long campaignId) {
    if (sectionGeoId == null && sectionNumber == null) {
      throw StructureException.badRequest("falta la sección: sectionGeoId o sectionNumber");
    }
    List<Object[]> rows =
        sectionGeoId != null
            ? reports.mobilizationBySection(sectionGeoId, campaigns.resolve(campaignId))
            : reports.mobilizationBySectionNumber(sectionNumber, campaigns.resolve(campaignId));
    if (rows.isEmpty()) {
      return Map.of("progress", 0L, "goal", 0L);
    }
    Object[] row = rows.get(0);
    return Map.of(
        "progress", ((Number) row[0]).longValue(),
        "goal", ((Number) row[1]).longValue());
  }
}
