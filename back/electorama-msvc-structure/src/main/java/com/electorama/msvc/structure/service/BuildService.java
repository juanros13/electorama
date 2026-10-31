package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.BuildRequests.AccountData;
import com.electorama.msvc.structure.model.dto.BuildRequests.AccountMode;
import com.electorama.msvc.structure.model.dto.BuildRequests.Appoint;
import com.electorama.msvc.structure.model.dto.BuildRequests.Appointed;
import com.electorama.msvc.structure.model.dto.BuildRequests.BranchSeat;
import com.electorama.msvc.structure.model.dto.BuildRequests.CreatePosition;
import com.electorama.msvc.structure.model.dto.BuildRequests.LinkPlan;
import com.electorama.msvc.structure.model.dto.BuildRequests.PersonData;
import com.electorama.msvc.structure.model.dto.BuildRequests.SectionMeta;
import com.electorama.msvc.structure.model.dto.BuildRequests.SectionSeat;
import com.electorama.msvc.structure.model.dto.BuildRequests.VacancyResult;
import com.electorama.msvc.structure.model.dto.RotateRequest;
import com.electorama.msvc.structure.model.entity.CampaignLevel;
import com.electorama.msvc.structure.model.entity.Person;
import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.enums.StructureType;
import com.electorama.msvc.structure.model.enums.TerritoryType;
import com.electorama.msvc.structure.model.projection.HeldPosition;
import com.electorama.msvc.structure.model.projection.PositionSummary;
import com.electorama.msvc.structure.model.repository.BuildRepository;
import com.electorama.msvc.structure.model.repository.BuildRepository.PositionRow;
import com.electorama.msvc.structure.model.repository.BuildRepository.StructureRow;
import com.electorama.msvc.structure.model.repository.BuildRepository.TerritoryRow;
import com.electorama.msvc.structure.model.repository.CampaignLevelRepository;
import com.electorama.msvc.structure.model.repository.PersonRepository;
import com.electorama.msvc.structure.model.repository.PositionRepository;
import com.electorama.msvc.structure.service.rest.UsersClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Armar la estructura de una campaña desde cero: crear puestos, generar las vacantes que pide la
 * meta y nombrar a sus titulares (con su cuenta).
 *
 * <p>Los niveles, sus nombres, dónde va cada uno y su meta los configura cada campaña ({@code
 * campaign_level}, V3). En 2024 y 2027: un coordinador (REFERENT) por municipio; un promotor por
 * cada 200 de meta de la sección; un movilizador por cada 20. Las reglas duras —el padre de rango
 * mayor, el territorio contenido en el del padre, no escribir en una campaña cerrada— las pone la
 * base.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

  private final BuildRepository build;
  private final CampaignLevelRepository levels;
  private final PositionService positions;
  private final PersonService personService;
  private final PersonRepository persons;
  private final AssignmentService assignments;
  private final UsersClient users;
  private final PositionRepository positionRepo;
  private final ObjectMapper json;

  // ---------------------------------------------------------------- crear

  /**
   * Crea un puesto de cualquier nivel de la campaña. El nivel va por {@code levelId} o, como antes,
   * por su función ({@code level}). La cúspide no cuelga de nadie; los demás cuelgan de un puesto
   * del nivel inmediato superior. El territorio sale del tipo del nivel.
   */
  @Transactional
  public PositionSummary create(CreatePosition r) {
    if (r == null) {
      throw StructureException.badRequest("faltan los datos del puesto");
    }
    PositionRow parent = r.parentPositionId() == null ? null : current(r.parentPositionId());
    if (parent != null
        && r.structureId() != null
        && !r.structureId().equals(parent.structureId())) {
      throw StructureException.badRequest("el puesto padre es de otra estructura");
    }
    StructureRow structure =
        writableStructure(parent != null ? parent.structureId() : r.structureId());
    List<CampaignLevel> all = levels.findByCampaignIdOrderByRank(structure.campaignId());
    CampaignLevel level = requestedLevel(all, r);

    CampaignLevel above = above(all, level);
    if (above == null && parent != null) {
      throw StructureException.badRequest(
          "un " + level.getNameSingular() + " no cuelga de nadie: es la cúspide");
    }
    if (above != null) {
      if (parent == null) {
        throw StructureException.badRequest(
            "falta el puesto de " + above.getNameSingular() + " del que cuelga");
      }
      if (!above.getId().equals(parent.levelId())) {
        throw StructureException.badRequest(
            "un "
                + level.getNameSingular()
                + " cuelga de un "
                + above.getNameSingular()
                + ", no de un "
                + levelOf(all, parent).getNameSingular());
      }
    }

    TerritoryRow territory = placement(level, r, parent, structure);
    if (level.getLevelFunction() == Level.REFERENT
        && build.hasPositionOn(structure.id(), territory.id(), level.getId())) {
      throw StructureException.conflict(
          "ese municipio ya tiene " + level.getNameSingular() + " en esta estructura");
    }
    long id =
        build.insertPosition(
            structure.id(),
            level.getId(),
            parent == null ? null : parent.id(),
            territory,
            r.goal() != null ? r.goal() : level.getDefaultGoal(),
            r.childLimit() != null ? r.childLimit() : level.getDefaultChildLimit(),
            prefix(level));
    return positions.get(id);
  }

  /** El nivel pedido: por id (de la campaña de la estructura) o por su función heredada. */
  static CampaignLevel requestedLevel(List<CampaignLevel> all, CreatePosition r) {
    if (r.levelId() != null) {
      return all.stream()
          .filter(l -> l.getId().equals(r.levelId()))
          .findFirst()
          .orElseThrow(
              () ->
                  StructureException.badRequest(
                      "el nivel " + r.levelId() + " no es de esta campaña"));
    }
    if (r.level() != null) {
      return all.stream()
          .filter(l -> l.getLevelFunction() == r.level())
          .findFirst()
          .orElseThrow(
              () -> StructureException.badRequest("la campaña no tiene un nivel " + r.level()));
    }
    throw StructureException.badRequest("falta el nivel del puesto");
  }

  /**
   * Dónde va el puesto. Un {@code territoryId} explícito manda; si no, lo decide el tipo del nivel:
   * el estado de la campaña, el municipio o la sección pedidos, o el del padre si ya es de ese
   * tipo.
   */
  private TerritoryRow placement(
      CampaignLevel level, CreatePosition r, PositionRow parent, StructureRow structure) {
    if (r.territoryId() != null) {
      return build
          .territoryById(r.territoryId())
          .orElseThrow(() -> StructureException.notFound("el territorio", r.territoryId()));
    }
    TerritoryType type = level.getTerritoryType();
    String name = level.getNameSingular();
    if (type == null) {
      if (r.sectionGeoId() != null) {
        return section(r.sectionGeoId());
      }
      if (r.municipalityGeoId() != null) {
        return municipality(r.municipalityGeoId());
      }
      if (parent != null) {
        return territoryOf(parent);
      }
      return campaignTerritory(structure, name);
    }
    return switch (type) {
      case STATE -> parent != null && "STATE".equals(parent.territoryType())
          ? territoryOf(parent)
          : campaignTerritory(structure, name);
      case MUNICIPALITY -> {
        if (r.municipalityGeoId() != null) {
          yield municipality(r.municipalityGeoId());
        }
        if (parent != null && "MUNICIPALITY".equals(parent.territoryType())) {
          yield territoryOf(parent);
        }
        throw StructureException.badRequest(
            "el " + name + " va sobre un municipio (municipalityGeoId)");
      }
      case SECTION -> {
        if (r.sectionGeoId() != null) {
          yield section(r.sectionGeoId());
        }
        if (parent != null && "SECTION".equals(parent.territoryType())) {
          yield territoryOf(parent);
        }
        throw StructureException.badRequest("el " + name + " va sobre una sección (sectionGeoId)");
      }
      case FRAGMENT -> throw StructureException.badRequest(
          "el " + name + " va sobre un fragmento de sección: indica su territoryId");
    };
  }

  private TerritoryRow campaignTerritory(StructureRow structure, String levelName) {
    if (structure.campaignTerritoryId() == null) {
      throw StructureException.badRequest(
          "la campaña no tiene territorio: indica dónde va el " + levelName + " (territoryId)");
    }
    return build
        .territoryById(structure.campaignTerritoryId())
        .orElseThrow(
            () -> StructureException.notFound("el territorio", structure.campaignTerritoryId()));
  }

  /** El prefijo del código del puesto; las funciones conservan el suyo (las compat lo buscan). */
  static String prefix(CampaignLevel level) {
    if (level.getLevelFunction() != null) {
      return switch (level.getLevelFunction()) {
        case REFERENT -> "REF";
        case ZONE_COORDINATOR -> "CZ";
        case PROMOTER -> "MOV";
      };
    }
    String code = level.getCode();
    return code.length() > 12 ? code.substring(0, 12) : code;
  }

  // ---------------------------------------------------------------- vacantes

  /**
   * Crea debajo del puesto las vacantes del nivel siguiente que faltan para cubrir la meta: una por
   * cada {@code defaultGoal} del nivel de abajo. Idempotente: cuenta los hijos vigentes y sólo crea
   * la diferencia. Va por sección: con {@code sections} (la meta de cada una) o, si el puesto ya
   * está sobre una sección, con su propia meta.
   */
  @Transactional
  public List<VacancyResult> vacancies(Long positionId, List<SectionMeta> sections) {
    PositionRow position = current(positionId);
    StructureRow structure = writableStructure(position.structureId());
    List<CampaignLevel> all = levels.findByCampaignIdOrderByRank(structure.campaignId());
    CampaignLevel level = levelOf(all, position);
    CampaignLevel child = below(all, level);
    if (child == null) {
      throw StructureException.badRequest(
          "un " + level.getNameSingular() + " no tiene puestos debajo");
    }
    Integer per = child.getDefaultGoal();
    if (per == null || per <= 0) {
      throw StructureException.badRequest(
          "configura la meta por defecto de "
              + child.getNamePlural()
              + " en la campaña para generar sus vacantes");
    }
    TerritoryType childType = child.getTerritoryType();
    if (childType != null && childType != TerritoryType.SECTION) {
      throw StructureException.badRequest(
          "las vacantes se generan por sección: crea los puestos de "
              + child.getNamePlural()
              + " uno por uno");
    }
    boolean bySection = sections != null && !sections.isEmpty();
    if (!bySection && !"SECTION".equals(position.territoryType())) {
      throw StructureException.badRequest("faltan las secciones con su meta");
    }

    List<VacancyResult> out = new ArrayList<>();
    if (bySection) {
      for (SectionMeta s : sections) {
        TerritoryRow section = section(s.sectionGeoId());
        out.add(fill(position, child, section, s.sectionGeoId(), s.meta(), per, section.id()));
      }
    } else {
      Integer goal = position.goal() != null ? position.goal() : level.getDefaultGoal();
      out.add(
          fill(position, child, territoryOf(position), position.sectionGeoId(), goal, per, null));
    }
    log.info(
        "vacantes bajo el puesto {}: {} creadas",
        positionId,
        out.stream().mapToInt(VacancyResult::created).sum());
    return out;
  }

  private VacancyResult fill(
      PositionRow parent,
      CampaignLevel child,
      TerritoryRow territory,
      Integer sectionGeoId,
      Integer meta,
      int per,
      Long countIn) {
    int needed = ceilDiv(meta, per);
    int existing = build.countChildren(parent.id(), child.getId(), countIn);
    int created = 0;
    for (int i = existing; i < needed; i++) {
      build.insertPosition(
          parent.structureId(),
          child.getId(),
          parent.id(),
          territory,
          per,
          child.getDefaultChildLimit(),
          prefix(child));
      created++;
    }
    return new VacancyResult(sectionGeoId, needed, existing, created);
  }

  // ---------------------------------------------------------------- nombrar

  /**
   * Nombra al titular. Si pide cuenta nueva, la crea en msvc-users con el rol de su nivel; si el
   * nombramiento falla después, la cuenta se borra para no dejarla suelta.
   */
  public Appointed appoint(Long positionId, Appoint request) {
    if (request == null || request.person() == null) {
      throw StructureException.badRequest("falta la persona que se nombra");
    }
    PositionRow position =
        build
            .position(positionId)
            .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));
    StructureRow structure = writableStructure(position.structureId());
    String role = roleOf(position, structure);

    // la persona se resuelve pero todavía no se guarda: si la cuenta falla (p.ej. ya existe), no
    // queda una persona suelta
    Person person = resolvePerson(request.person());
    AccountData account =
        request.account() == null
            ? new AccountData(AccountMode.NONE, null, null, null)
            : request.account();

    String authId = null;
    boolean created = false;
    LinkPlan plan = null;
    switch (account.mode() == null ? AccountMode.NONE : account.mode()) {
      case CREATE -> {
        authId = createAccount(person, account, role);
        created = true;
        // el correo con el que entra queda también en la persona si no tenía: es el que se
        // muestra en el puesto
        if (isBlank(person.getEmail()) && !isBlank(account.email())) {
          person.setEmail(account.email().trim().toLowerCase());
          if (person.getId() != null) {
            person = persons.save(person);
          }
        }
      }
      case LINK -> {
        if (account.authId() == null || account.authId().isBlank()) {
          throw StructureException.badRequest("para ligar una cuenta falta su authId");
        }
        authId = account.authId();
        plan = linkPlan(positionId, authId);
        if (plan.hasConflict() && !Boolean.TRUE.equals(request.replaceInBranch())) {
          throw new StructureException(
              HttpStatus.CONFLICT,
              "la cuenta ya ocupa otro puesto en esta rama: confirma el cambio",
              Map.of("code", "BRANCH_CONFLICT", "plan", plan));
        }
        // la cuenta ligada recibe el rol del nivel: sin él ocupa el puesto pero no ve sus
        // pantallas (pasó con un promotor de 2024 nombrado referente en 2027)
        try {
          users.grantRole(authId, new UsersClient.RoleGrant(role));
        } catch (FeignException e) {
          throw fromUsers(e);
        }
      }
      case NONE -> authId = null;
    }

    // «toda la sección»: los demás puestos vacantes de promotor de la sección, para la misma
    // persona
    List<Long> alsoFill =
        Boolean.TRUE.equals(request.wholeSection())
                && Level.ZONE_COORDINATOR.name().equals(position.level())
            ? build.vacantSectionSiblings(positionId).stream().map(SectionSeat::positionId).toList()
            : List.of();

    try {
      if (person.getId() == null) {
        person = personService.create(person);
      }
      assignments.vacateAndRotate(
          plan == null ? List.of() : plan.vacates().stream().map(BranchSeat::positionId).toList(),
          "pasa a otro puesto de la rama (" + positionId + ")",
          positionId,
          alsoFill,
          new RotateRequest(
              person.getId(),
              authId,
              request.reason() == null ? "nombramiento" : request.reason()));
    } catch (RuntimeException e) {
      if (created) {
        log.warn("el nombramiento falló; se borra la cuenta recién creada {}", authId);
        try {
          users.delete(authId);
        } catch (RuntimeException cleanup) {
          log.error("no se pudo borrar la cuenta {}: quedó suelta en Keycloak", authId, cleanup);
        }
      }
      throw e;
    }
    log.info(
        "puesto {} nombrado: persona {}, cuenta {} ({})",
        positionId,
        person.getId(),
        authId,
        created ? "nueva" : "existente o sin cuenta");
    if (!alsoFill.isEmpty()) {
      log.info("la misma persona ocupa también {} (toda la sección)", alsoFill);
    }
    List<String> revoked = plan == null ? List.of() : revokeRoles(authId, plan.rolesToRevoke());
    return new Appointed(
        positionId,
        person.getId(),
        authId,
        created,
        role,
        plan == null ? List.of() : plan.vacates().stream().map(BranchSeat::positionId).toList(),
        revoked,
        alsoFill);
  }

  /** Los demás puestos vacantes de promotor de la sección del puesto («toda la sección»). */
  @Transactional(readOnly = true)
  public List<SectionSeat> sectionVacancies(Long positionId) {
    return build.vacantSectionSiblings(positionId);
  }

  /**
   * Qué pasa si se liga la cuenta al puesto: recibe el rol del nivel y, si ya ocupa otros puestos
   * en la MISMA rama (mismo referente raíz), ésos quedan vacantes y se le quitan los roles que ya
   * no pide ningún puesto que conserve en una campaña abierta. Lo de otras ramas no se toca.
   */
  @Transactional(readOnly = true)
  public LinkPlan linkPlan(Long positionId, String authId) {
    PositionRow position =
        build
            .position(positionId)
            .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));
    StructureRow structure = writableStructure(position.structureId());
    String grant = roleOf(position, structure);
    Long root = positionRepo.rootOf(positionId).orElse(positionId);

    List<HeldPosition> held = positionRepo.heldWithRoot(authId);
    List<HeldPosition> sameBranch =
        held.stream()
            .filter(h -> !h.getPositionId().equals(positionId))
            .filter(h -> Objects.equals(h.getRootId(), root))
            // un promotor puede llevar varias secciones: ocupar otro puesto de promotor de la
            // rama no lo saca de los que ya tiene
            .filter(
                h ->
                    !(Level.ZONE_COORDINATOR.name().equals(position.level())
                        && Level.ZONE_COORDINATOR.name().equals(h.getLevel())))
            .toList();
    Set<Long> leaving =
        sameBranch.stream().map(HeldPosition::getPositionId).collect(Collectors.toSet());
    Set<String> stillNeeded =
        held.stream()
            .filter(h -> !leaving.contains(h.getPositionId()))
            .filter(h -> !h.getPositionId().equals(positionId))
            .filter(h -> !"CLOSED".equals(h.getCampaignStatus()))
            .map(BuildService::roleOf)
            .collect(Collectors.toSet());
    stillNeeded.add(grant);
    List<String> revoke =
        sameBranch.stream()
            .map(BuildService::roleOf)
            .filter(r -> !stillNeeded.contains(r))
            .distinct()
            .sorted()
            .toList();
    List<BranchSeat> seats =
        sameBranch.stream()
            .map(
                h ->
                    new BranchSeat(
                        h.getPositionId(),
                        h.getLevel(),
                        h.getSectionKey() != null ? h.getSectionKey() : h.getTerritoryCode()))
            .toList();
    return new LinkPlan(grant, seats, revoke);
  }

  /**
   * Quita los roles en msvc-users. Va después del nombramiento: si Keycloak falla, el puesto ya
   * quedó bien y sólo sobra un rol, que se registra para quitarlo a mano.
   */
  private List<String> revokeRoles(String authId, List<String> roles) {
    List<String> done = new ArrayList<>();
    for (String r : roles) {
      try {
        users.revokeRole(authId, r);
        done.add(r);
      } catch (RuntimeException e) {
        log.error("no se pudo quitar el rol {} a la cuenta {}: quitarlo a mano", r, authId, e);
      }
    }
    return done;
  }

  /** La persona existente (por id o CURP) o una nueva SIN guardar (id null). */
  private Person resolvePerson(PersonData data) {
    if (data.personId() != null) {
      return persons
          .findById(data.personId())
          .orElseThrow(() -> StructureException.notFound("la persona", data.personId()));
    }
    if (isBlank(data.names()) || isBlank(data.lastName())) {
      throw StructureException.badRequest("faltan el nombre y el primer apellido");
    }
    if (!isBlank(data.curp())) {
      var existing = persons.findByCurpIgnoreCase(data.curp().trim());
      if (existing.isPresent()) {
        return existing.get();
      }
    }
    return Person.builder()
        .names(data.names().trim().toUpperCase())
        .lastName(data.lastName().trim().toUpperCase())
        .surName(isBlank(data.surName()) ? null : data.surName().trim().toUpperCase())
        .curp(isBlank(data.curp()) ? null : data.curp().trim().toUpperCase())
        .ine(isBlank(data.ine()) ? null : data.ine().trim())
        .cellPhoneNumber(isBlank(data.phone()) ? null : data.phone().trim())
        .email(isBlank(data.email()) ? null : data.email().trim().toLowerCase())
        .legacySource("structure-2027")
        .build();
  }

  private String createAccount(Person person, AccountData account, String role) {
    String email = !isBlank(account.email()) ? account.email() : person.getEmail();
    if (isBlank(email) || isBlank(account.password())) {
      throw StructureException.badRequest("para crear la cuenta faltan el correo y la contraseña");
    }
    String lastName =
        person.getSurName() == null
            ? person.getLastName()
            : person.getLastName() + " " + person.getSurName();
    try {
      return users
          .create(
              new UsersClient.AccountRequest(
                  email.trim().toLowerCase(),
                  account.password(),
                  person.getNames(),
                  lastName,
                  role))
          .authId();
    } catch (FeignException e) {
      throw fromUsers(e);
    }
  }

  /** El error de msvc-users con su mismo código; un 409 lleva el authId de la cuenta existente. */
  private StructureException fromUsers(FeignException e) {
    HttpStatus status = HttpStatus.resolve(e.status());
    String message = "msvc-users respondió " + e.status();
    Map<String, Object> detail = null;
    try {
      JsonNode body = json.readTree(e.contentUTF8());
      if (body.hasNonNull("message")) {
        message = body.get("message").asText();
      }
      if (body.hasNonNull("detail")) {
        detail = json.convertValue(body.get("detail"), Map.class);
      }
    } catch (Exception ignored) {
      // cuerpo vacío o no JSON: se queda el mensaje genérico
    }
    if (status == null || status.is5xxServerError() || e.status() <= 0) {
      return new StructureException(
          HttpStatus.BAD_GATEWAY, "no se pudo crear la cuenta: " + message);
    }
    return new StructureException(status, message, detail);
  }

  // ---------------------------------------------------------------- utilidades

  /**
   * El rol del realm que le toca al titular del puesto: el de su nivel (el alterno en la alterna).
   */
  private String roleOf(PositionRow position, StructureRow structure) {
    CampaignLevel level =
        levels
            .findById(position.levelId())
            .orElseThrow(() -> StructureException.notFound("el nivel", position.levelId()));
    return level.roleFor(StructureType.valueOf(structure.type()));
  }

  /** El rol de un puesto que ya ocupa la cuenta, con los nombres que trae la consulta. */
  static String roleOf(HeldPosition h) {
    return "ALTERNATE".equals(h.getStructureType()) && h.getAlternateRoleName() != null
        ? h.getAlternateRoleName()
        : h.getRoleName();
  }

  private PositionRow current(Long positionId) {
    return build
        .position(positionId)
        .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));
  }

  private static CampaignLevel levelOf(List<CampaignLevel> all, PositionRow position) {
    return all.stream()
        .filter(l -> l.getId().equals(position.levelId()))
        .findFirst()
        .orElseThrow(() -> StructureException.notFound("el nivel", position.levelId()));
  }

  /** El nivel inmediato superior; null en la cúspide. */
  static CampaignLevel above(List<CampaignLevel> all, CampaignLevel level) {
    CampaignLevel found = null;
    for (CampaignLevel l : all) {
      if (l.getRank() < level.getRank() && (found == null || l.getRank() > found.getRank())) {
        found = l;
      }
    }
    return found;
  }

  /** El nivel inmediato inferior; null en el último. */
  static CampaignLevel below(List<CampaignLevel> all, CampaignLevel level) {
    CampaignLevel found = null;
    for (CampaignLevel l : all) {
      if (l.getRank() > level.getRank() && (found == null || l.getRank() < found.getRank())) {
        found = l;
      }
    }
    return found;
  }

  private StructureRow writableStructure(Long structureId) {
    if (structureId == null) {
      throw StructureException.badRequest("falta la estructura");
    }
    StructureRow structure =
        build
            .structure(structureId)
            .orElseThrow(() -> StructureException.notFound("la estructura", structureId));
    if ("CLOSED".equals(structure.campaignStatus())) {
      throw StructureException.conflict(
          "la campaña de esa estructura está cerrada: sólo se consulta");
    }
    return structure;
  }

  private TerritoryRow section(Integer sectionGeoId) {
    return build
        .sectionTerritory(sectionGeoId)
        .orElseThrow(() -> StructureException.notFound("la sección", sectionGeoId));
  }

  private TerritoryRow municipality(Integer municipalityGeoId) {
    return build
        .municipalityTerritory(municipalityGeoId)
        .orElseThrow(() -> StructureException.notFound("el municipio", municipalityGeoId));
  }

  private TerritoryRow territoryOf(PositionRow position) {
    return new TerritoryRow(
        position.territoryId(),
        position.territoryType(),
        position.territoryCode(),
        position.sectionGeoId(),
        position.municipalityGeoId());
  }

  private static int ceilDiv(Integer value, int by) {
    if (value == null || value <= 0) {
      return 0;
    }
    return (value + by - 1) / by;
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
