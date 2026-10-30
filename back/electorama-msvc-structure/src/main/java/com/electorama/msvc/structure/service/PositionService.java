package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.entity.Position;
import com.electorama.msvc.structure.model.projection.DailyActivity;
import com.electorama.msvc.structure.model.projection.MunicipalityPosition;
import com.electorama.msvc.structure.model.projection.MunicipalitySummary;
import com.electorama.msvc.structure.model.projection.PositionSummary;
import com.electorama.msvc.structure.model.projection.SectionGoal;
import com.electorama.msvc.structure.model.projection.SectionOverview;
import com.electorama.msvc.structure.model.projection.TerritoryScope;
import com.electorama.msvc.structure.model.repository.MembershipRepository;
import com.electorama.msvc.structure.model.repository.PositionRepository;
import com.electorama.msvc.structure.security.BranchGuard;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consultas y cambios sobre el esqueleto: el puesto, su meta y su cierre. */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionService {

  private final PositionRepository positions;
  private final MembershipRepository memberships;
  private final CampaignService campaigns;
  private final BranchGuard branch;

  @Transactional(readOnly = true)
  public PositionSummary get(Long id) {
    return positions.summary(id).orElseThrow(() -> StructureException.notFound("el puesto", id));
  }

  @Transactional(readOnly = true)
  public List<PositionSummary> children(Long id) {
    get(id);
    return positions.children(id);
  }

  /** Los referentes de la estructura: todos para ADMIN; para un referente, sólo los suyos. */
  @Transactional(readOnly = true)
  public List<PositionSummary> roots(Long structureId) {
    List<PositionSummary> all = positions.roots(structureId);
    if (branch.isAdmin()) {
      return all;
    }
    String authId = branch.callerAuthId();
    if (!branch.isStructureMember() || authId == null) {
      return List.of();
    }
    // sus puestos, los de más arriba: si ocupa uno que cuelga de otro suyo, ése ya se ve en la rama
    List<Long> held = positions.positionsHeldIn(authId, structureId);
    return held.stream()
        .filter(id -> !positions.inHeldBranch(authId, id, false))
        .map(this::get)
        .toList();
  }

  @Transactional(readOnly = true)
  public List<PositionSummary> bySection(Integer sectionGeoId, Long campaignId) {
    return positions.bySection(sectionGeoId, campaigns.resolve(campaignId));
  }

  /** La meta repartida por sección; las secciones sin coordinadores no vienen. */
  @Transactional(readOnly = true)
  public List<SectionGoal> sectionGoals(List<Integer> sectionGeoIds, Long campaignId) {
    if (sectionGeoIds == null || sectionGeoIds.isEmpty()) {
      return List.of();
    }
    return positions.sectionGoals(sectionGeoIds, campaigns.resolve(campaignId));
  }

  /** Todas las secciones con estructura: meta repartida, gente, convencidos y coordinadores. */
  @Transactional(readOnly = true)
  public List<SectionOverview> sectionOverview(Long campaignId) {
    return positions.sectionOverview(campaigns.resolve(campaignId));
  }

  @Transactional(readOnly = true)
  public List<MunicipalitySummary> municipalitySummary(Long campaignId) {
    return positions.municipalitySummary(campaigns.resolve(campaignId));
  }

  @Transactional(readOnly = true)
  public List<MunicipalityPosition> municipalityPositions(
      Integer municipalityGeoId, Long campaignId) {
    return positions.municipalityPositions(municipalityGeoId, campaigns.resolve(campaignId));
  }

  @Transactional(readOnly = true)
  public List<TerritoryScope> scopeOf(String authId, Long campaignId) {
    return positions.scopeOf(authId, campaigns.resolve(campaignId));
  }

  /** Los convencidos de todo el subárbol, que es la pregunta real del avance contra la meta. */
  @Transactional(readOnly = true)
  public long subtreeMemberships(Long id) {
    get(id);
    return memberships.countInSubtree(id);
  }

  /** El rango más largo que se pinta: dos años de días ya no se leen en una gráfica de barras. */
  private static final long MAX_ACTIVITY_DAYS = 731;

  /** La actividad por día del subárbol, para la gráfica del perfil. */
  @Transactional(readOnly = true)
  public List<DailyActivity> activity(Long id, LocalDate from, LocalDate to) {
    get(id);
    if (from != null && to != null) {
      if (to.isBefore(from)) {
        throw new StructureException(HttpStatus.BAD_REQUEST, "el rango termina antes de empezar");
      }
      if (ChronoUnit.DAYS.between(from, to) > MAX_ACTIVITY_DAYS) {
        throw new StructureException(
            HttpStatus.BAD_REQUEST, "el rango es de a lo más " + MAX_ACTIVITY_DAYS + " días");
      }
    }
    List<DailyActivity> days =
        memberships.dailyInSubtree(
            id, from == null ? null : from.toString(), to == null ? null : to.toString());
    // sin rango pedido, el de los datos: se recorta a los últimos MAX_ACTIVITY_DAYS
    return days.size() > MAX_ACTIVITY_DAYS
        ? days.subList(days.size() - (int) MAX_ACTIVITY_DAYS, days.size())
        : days;
  }

  /** La meta y el tope de hijos viven en el puesto, no en el territorio ni en la persona. */
  @Transactional
  public PositionSummary updateGoal(Long id, Integer goal, Integer childLimit) {
    Position position =
        positions
            .findByIdAndValidToIsNull(id)
            .orElseThrow(() -> StructureException.notFound("el puesto vigente", id));
    if (goal != null && goal < 0) {
      throw StructureException.badRequest("la meta no puede ser negativa");
    }
    position.setGoal(goal);
    position.setChildLimit(childLimit);
    positions.save(position);
    return get(id);
  }

  /**
   * Cierra el puesto.
   *
   * <p>Se niega si todavía le cuelga alguien: cerrar un coordinador con promotores debajo los
   * dejaría sin padre. Es el mismo guard que el modelo viejo tenía en {@code deleteByPromoters},
   * pero aquí cubre cualquier nivel.
   */
  @Transactional
  public void close(Long id) {
    positions
        .findByIdAndValidToIsNull(id)
        .orElseThrow(() -> StructureException.notFound("el puesto vigente", id));

    long children = positions.countCurrentChildren(id);
    if (children > 0) {
      throw StructureException.conflict(
          "el puesto todavía tiene "
              + children
              + " puesto(s) debajo: reubícalos antes de cerrarlo");
    }
    positions.close(id);
    log.info("puesto {} cerrado", id);
  }
}
