package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.CampaignRequests.CampaignData;
import com.electorama.msvc.structure.model.dto.CampaignRequests.ElectionData;
import com.electorama.msvc.structure.model.dto.CampaignRequests.LevelData;
import com.electorama.msvc.structure.model.dto.CampaignRequests.StructureData;
import com.electorama.msvc.structure.model.dto.CampaignSummary;
import com.electorama.msvc.structure.model.entity.Campaign;
import com.electorama.msvc.structure.model.entity.CampaignElection;
import com.electorama.msvc.structure.model.entity.CampaignLevel;
import com.electorama.msvc.structure.model.entity.Structure;
import com.electorama.msvc.structure.model.entity.Territory;
import com.electorama.msvc.structure.model.enums.CampaignStatus;
import com.electorama.msvc.structure.model.enums.CampaignType;
import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.enums.StructureType;
import com.electorama.msvc.structure.model.repository.CampaignElectionRepository;
import com.electorama.msvc.structure.model.repository.CampaignLevelRepository;
import com.electorama.msvc.structure.model.repository.CampaignRepository;
import com.electorama.msvc.structure.model.repository.StructureRepository;
import com.electorama.msvc.structure.model.repository.TerritoryRepository;
import com.electorama.msvc.structure.service.LevelRules.Planned;
import com.electorama.msvc.structure.service.rest.UsersClient;
import feign.FeignException;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Configurar una campaña: datos generales, estatus, cargos en disputa, estructuras y niveles del
 * organigrama. Sólo ADMIN (lo pide el controlador).
 *
 * <p>Una campaña cerrada ya no se edita: V6 le congela la estructura y aquí también la
 * configuración.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CampaignAdminService {

  static final Set<String> OFFICES =
      Set.of("PRESIDENT", "SENATOR", "FEDERAL_DEPUTY", "GOVERNOR", "LOCAL_DEPUTY", "MAYOR");
  static final Set<String> SCOPES =
      Set.of("STATE", "FEDERAL_DISTRICT", "LOCAL_DISTRICT", "MUNICIPALITY");

  private static final BigDecimal DEFAULT_GOAL_PERCENTAGE = BigDecimal.valueOf(61);
  private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

  /** El rol de estructura de cada función, para copiarle sus menús a un rol nuevo. */
  private static final Map<Level, String[]> LEGACY_ROLE =
      Map.of(
          Level.REFERENT, new String[] {"CORDINATOR_MUNICIPAL", "CORDINATOR_MUNICIPAL_ALT"},
          Level.ZONE_COORDINATOR, new String[] {"CORDINATOR_ZONE", "CORDINATOR_ZONE_ALT"},
          Level.PROMOTER, new String[] {"PROMOTER", "PROMOTER"});

  private final CampaignRepository campaigns;
  private final CampaignLevelRepository levels;
  private final CampaignElectionRepository elections;
  private final StructureRepository structures;
  private final TerritoryRepository territories;
  private final CampaignService campaignService;
  private final UsersClient users;
  private final EntityManager em;

  // ---------------------------------------------------------------- datos generales

  /** Crea la campaña en PLANNING con su estructura Principal; los niveles se configuran después. */
  @Transactional
  public CampaignSummary create(CampaignData data) {
    Campaign c = new Campaign();
    c.setStatus(CampaignStatus.PLANNING);
    apply(c, data);
    c = campaigns.save(c);
    structures.save(
        Structure.builder().campaign(c).name("Principal").type(StructureType.MAIN).build());
    log.info("campaña creada: {} ({})", c.getName(), c.getId());
    return campaignService.detail(c.getId());
  }

  @Transactional
  public CampaignSummary update(Long id, CampaignData data) {
    Campaign c = writable(id);
    apply(c, data);
    return campaignService.detail(campaigns.save(c).getId());
  }

  /**
   * PLANNING → ACTIVE → CLOSED, sin vuelta atrás. Para activar hacen falta niveles y estructura, y
   * que no haya otra campaña activa en el mismo territorio (V6).
   */
  @Transactional
  public CampaignSummary changeStatus(Long id, String status) {
    Campaign c = campaign(id);
    CampaignStatus target = parse(CampaignStatus.class, status, "estatus");
    CampaignStatus current = c.getStatus();
    boolean allowed =
        (current == CampaignStatus.PLANNING && target != CampaignStatus.PLANNING)
            || (current == CampaignStatus.ACTIVE && target == CampaignStatus.CLOSED);
    if (target == current) {
      return campaignService.detail(id);
    }
    if (!allowed) {
      throw StructureException.conflict(
          "una campaña "
              + current
              + " no pasa a "
              + target
              + ": el orden es PLANNING → ACTIVE → CLOSED");
    }
    if (target == CampaignStatus.ACTIVE) {
      if (levels.findByCampaignIdOrderByRank(id).isEmpty()) {
        throw StructureException.conflict("antes de activarla, configura sus niveles");
      }
      if (structures.findByCampaignIdOrderById(id).isEmpty()) {
        throw StructureException.conflict("antes de activarla, agrega una estructura");
      }
      Long territoryId = c.getTerritory() == null ? null : c.getTerritory().getId();
      boolean otherActive =
          campaigns.findAll().stream()
              .anyMatch(
                  o ->
                      !o.getId().equals(id)
                          && o.getStatus() == CampaignStatus.ACTIVE
                          && java.util.Objects.equals(
                              o.getTerritory() == null ? null : o.getTerritory().getId(),
                              territoryId));
      if (otherActive) {
        throw StructureException.conflict(
            "ya hay una campaña activa en ese territorio: ciérrala antes de activar ésta");
      }
    }
    c.setStatus(target);
    campaigns.save(c);
    log.info("campaña {} pasa de {} a {}", id, current, target);
    return campaignService.detail(id);
  }

  private void apply(Campaign c, CampaignData d) {
    if (d == null || d.name() == null || d.name().isBlank()) {
      throw StructureException.badRequest("la campaña necesita un nombre");
    }
    c.setName(d.name().trim());
    c.setType(d.type() == null ? CampaignType.STATE : parse(CampaignType.class, d.type(), "tipo"));
    c.setYear(
        d.year() != null ? d.year() : d.electionDay() == null ? null : d.electionDay().getYear());
    c.setElectionDay(d.electionDay());
    c.setStartDate(d.startDate());
    c.setEndDate(d.endDate());
    if (d.startDate() != null && d.endDate() != null && d.endDate().isBefore(d.startDate())) {
      throw StructureException.badRequest("la campaña no puede cerrar antes de empezar");
    }
    c.setDescription(
        d.description() == null || d.description().isBlank() ? null : d.description().trim());
    if (d.goalPercentage() != null) {
      if (d.goalPercentage().signum() <= 0 || d.goalPercentage().compareTo(HUNDRED) > 0) {
        throw StructureException.badRequest("el % de meta va de más de 0 a 100");
      }
      c.setGoalPercentage(d.goalPercentage());
    } else if (c.getGoalPercentage() == null) {
      c.setGoalPercentage(DEFAULT_GOAL_PERCENTAGE);
    }
    if (d.territoryId() == null) {
      c.setTerritory(null);
    } else {
      Territory t =
          territories
              .findById(d.territoryId())
              .orElseThrow(() -> StructureException.notFound("el territorio", d.territoryId()));
      c.setTerritory(t);
    }
  }

  // ---------------------------------------------------------------- niveles

  /**
   * Reemplaza los niveles por la lista mandada (el primero es la cúspide). Primero se aseguran los
   * roles en Keycloak: si eso falla no se toca la base, y un rol de sobra no estorba.
   */
  @Transactional
  public CampaignSummary replaceLevels(Long id, List<LevelData> requested) {
    Campaign c = writable(id);
    List<CampaignLevel> current = levels.findByCampaignIdOrderByRank(id);
    Map<Long, Long> usage = new HashMap<>();
    levels.usage(id).forEach(u -> usage.put(u.getLevelId(), u.getPositions()));
    List<Planned> plan = LevelRules.plan(requested, current, usage, LevelRules.slug(c.getName()));

    ensureRoles(c, plan);

    // los rangos se intercambian al reordenar: la unicidad se revisa al final de la transacción
    em.createNativeQuery("SET CONSTRAINTS structure.campaign_level_rank_uq DEFERRED")
        .executeUpdate();
    Set<Long> kept = new HashSet<>();
    Map<Long, CampaignLevel> byId = new HashMap<>();
    current.forEach(l -> byId.put(l.getId(), l));
    for (Planned p : plan) {
      CampaignLevel l = p.id() == null ? new CampaignLevel() : byId.get(p.id());
      l.setCampaignId(id);
      l.setRank(p.rank());
      l.setCode(p.code());
      l.setNameSingular(p.nameSingular());
      l.setNamePlural(p.namePlural());
      l.setTerritoryType(p.territoryType());
      l.setDefaultGoal(p.defaultGoal());
      l.setDefaultChildLimit(p.defaultChildLimit());
      l.setLevelFunction(p.function());
      l.setRoleName(p.roleName());
      l.setAlternateRoleName(p.alternateRoleName());
      kept.add(levels.save(l).getId());
    }
    current.stream().filter(l -> !kept.contains(l.getId())).forEach(levels::delete);
    levels.flush();
    em.createNativeQuery("SET CONSTRAINTS structure.campaign_level_rank_uq IMMEDIATE")
        .executeUpdate();
    log.info("campaña {}: {} niveles", id, plan.size());
    return campaignService.detail(id);
  }

  /**
   * Crea (si faltan) los roles de cada nivel en el realm y los registra en msvc-users con su
   * antigüedad, para que allá sepa qué roles da la estructura y quién administra a quién.
   */
  private void ensureRoles(Campaign c, List<Planned> plan) {
    int count = plan.size();
    for (Planned p : plan) {
      int seniority = count - p.rank() + 1;
      String[] legacy = p.function() == null ? null : LEGACY_ROLE.get(p.function());
      ensureRole(c, p.roleName(), seniority, p.nameSingular(), legacy == null ? null : legacy[0]);
      if (p.alternateRoleName() != null) {
        ensureRole(
            c,
            p.alternateRoleName(),
            seniority,
            p.nameSingular() + " (alterna)",
            legacy == null ? null : legacy[1]);
      }
    }
  }

  private void ensureRole(Campaign c, String role, int seniority, String level, String copyFrom) {
    try {
      users.ensureRole(
          new UsersClient.RoleSpec(
              role, seniority, c.getId(), c.getName() + ": " + level, copyFrom));
    } catch (FeignException e) {
      throw new StructureException(
          HttpStatus.BAD_GATEWAY,
          "no se pudo crear el rol " + role + " en msvc-users (" + e.status() + ")");
    }
  }

  // ---------------------------------------------------------------- cargos

  @Transactional
  public CampaignSummary replaceElections(Long id, List<ElectionData> requested) {
    Campaign c = writable(id);
    Set<String> seen = new HashSet<>();
    List<CampaignElection> rows =
        (requested == null ? List.<ElectionData>of() : requested)
            .stream()
                .map(
                    e -> {
                      String office = upper(e.office());
                      String scope = upper(e.scope());
                      if (!OFFICES.contains(office)) {
                        throw StructureException.badRequest("cargo desconocido: " + e.office());
                      }
                      if (!SCOPES.contains(scope)) {
                        throw StructureException.badRequest("ámbito desconocido: " + e.scope());
                      }
                      if (!seen.add(office)) {
                        throw StructureException.badRequest("el cargo " + office + " va dos veces");
                      }
                      if (e.seats() == null || e.seats() < 1) {
                        throw StructureException.badRequest(
                            "el cargo " + office + " necesita cuántos se eligen");
                      }
                      var day = e.electionDay() != null ? e.electionDay() : c.getElectionDay();
                      if (day == null) {
                        throw StructureException.badRequest(
                            "falta el día de la elección (en el cargo o en la campaña)");
                      }
                      return CampaignElection.builder()
                          .campaignId(id)
                          .office(office)
                          .scope(scope)
                          .seats(e.seats())
                          .electionDay(day)
                          .build();
                    })
                .toList();
    elections.deleteByCampaignId(id);
    elections.flush();
    elections.saveAll(rows);
    return campaignService.detail(id);
  }

  // ---------------------------------------------------------------- estructuras

  @Transactional
  public CampaignSummary addStructure(Long campaignId, StructureData data) {
    Campaign c = writable(campaignId);
    structures.save(
        Structure.builder()
            .campaign(c)
            .name(structureName(data))
            .type(structureType(data))
            .build());
    return campaignService.detail(campaignId);
  }

  @Transactional
  public CampaignSummary updateStructure(Long structureId, StructureData data) {
    Structure s = structure(structureId);
    writable(s.getCampaign().getId());
    s.setName(structureName(data));
    s.setType(structureType(data));
    structures.save(s);
    return campaignService.detail(s.getCampaign().getId());
  }

  /** Sólo una estructura sin puestos (vigentes o históricos) se borra. */
  @Transactional
  public CampaignSummary deleteStructure(Long structureId) {
    Structure s = structure(structureId);
    Long campaignId = s.getCampaign().getId();
    writable(campaignId);
    Number used =
        (Number)
            em.createNativeQuery("SELECT count(*) FROM position WHERE structure_id = :id")
                .setParameter("id", structureId)
                .getSingleResult();
    if (used.longValue() > 0) {
      throw StructureException.conflict("la estructura " + s.getName() + " ya tiene puestos");
    }
    structures.delete(s);
    return campaignService.detail(campaignId);
  }

  private String structureName(StructureData d) {
    if (d == null || d.name() == null || d.name().isBlank()) {
      throw StructureException.badRequest("la estructura necesita un nombre");
    }
    return d.name().trim();
  }

  private StructureType structureType(StructureData d) {
    return d.type() == null
        ? StructureType.MAIN
        : parse(StructureType.class, d.type(), "tipo de estructura");
  }

  // ---------------------------------------------------------------- utilidades

  private Campaign campaign(Long id) {
    return campaigns.findById(id).orElseThrow(() -> StructureException.notFound("la campaña", id));
  }

  private Campaign writable(Long id) {
    Campaign c = campaign(id);
    if (c.getStatus() == CampaignStatus.CLOSED) {
      throw StructureException.conflict("la campaña está cerrada: sólo se consulta");
    }
    return c;
  }

  private Structure structure(Long id) {
    return structures
        .findById(id)
        .orElseThrow(() -> StructureException.notFound("la estructura", id));
  }

  private static String upper(String s) {
    return s == null ? "" : s.trim().toUpperCase(Locale.ROOT);
  }

  private static <E extends Enum<E>> E parse(Class<E> type, String value, String what) {
    try {
      return Enum.valueOf(type, upper(value));
    } catch (IllegalArgumentException e) {
      throw StructureException.badRequest(what + " desconocido: " + value);
    }
  }
}
