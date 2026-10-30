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
import com.electorama.msvc.structure.model.entity.Person;
import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.projection.HeldPosition;
import com.electorama.msvc.structure.model.projection.PositionSummary;
import com.electorama.msvc.structure.model.repository.BuildRepository;
import com.electorama.msvc.structure.model.repository.BuildRepository.PositionRow;
import com.electorama.msvc.structure.model.repository.BuildRepository.StructureRow;
import com.electorama.msvc.structure.model.repository.BuildRepository.TerritoryRow;
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
 * <p>Las reglas de la campaña (Excel "estructura estatal"): un referente por municipio; un
 * "promotor" (coordinador de zona) por cada 200 de meta de la sección; un "movilizador" (promotor)
 * por cada 20. Las reglas duras —el padre de rango mayor, el territorio contenido en el del padre,
 * no escribir en una campaña cerrada— las pone la base.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BuildService {

  public static final int META_PER_COORDINATOR = 200;
  public static final int META_PER_PROMOTER = 20;

  private final BuildRepository build;
  private final PositionService positions;
  private final PersonService personService;
  private final PersonRepository persons;
  private final AssignmentService assignments;
  private final UsersClient users;
  private final PositionRepository positionRepo;
  private final ObjectMapper json;

  // ---------------------------------------------------------------- crear

  @Transactional
  public PositionSummary create(CreatePosition request) {
    if (request == null || request.level() == null) {
      throw StructureException.badRequest("falta el nivel del puesto");
    }
    return switch (request.level()) {
      case REFERENT -> positions.get(createReferent(request));
      case ZONE_COORDINATOR -> positions.get(createCoordinator(request));
      case PROMOTER -> positions.get(createPromoter(request));
    };
  }

  private long createReferent(CreatePosition r) {
    StructureRow structure = writableStructure(r.structureId());
    if (r.parentPositionId() != null) {
      throw StructureException.badRequest("un referente no cuelga de nadie");
    }
    if (r.municipalityGeoId() == null) {
      throw StructureException.badRequest("el referente va sobre un municipio (municipalityGeoId)");
    }
    TerritoryRow municipality =
        build
            .municipalityTerritory(r.municipalityGeoId())
            .orElseThrow(() -> StructureException.notFound("el municipio", r.municipalityGeoId()));
    if (build.hasReferent(structure.id(), municipality.id())) {
      throw StructureException.conflict("ese municipio ya tiene referente en esta estructura");
    }
    return build.insertPosition(
        structure.id(), "REFERENT", null, municipality, r.goal(), r.childLimit(), "REF");
  }

  private long createCoordinator(CreatePosition r) {
    PositionRow referent = parent(r.parentPositionId(), Level.REFERENT);
    if (r.sectionGeoId() == null) {
      throw StructureException.badRequest("el promotor va sobre una sección (sectionGeoId)");
    }
    TerritoryRow section = section(r.sectionGeoId());
    int goal = r.goal() == null ? META_PER_COORDINATOR : r.goal();
    return build.insertPosition(
        referent.structureId(),
        "ZONE_COORDINATOR",
        referent.id(),
        section,
        goal,
        r.childLimit(),
        "CZ");
  }

  private long createPromoter(CreatePosition r) {
    PositionRow coordinator = parent(r.parentPositionId(), Level.ZONE_COORDINATOR);
    TerritoryRow section =
        r.sectionGeoId() == null ? territoryOf(coordinator) : section(r.sectionGeoId());
    int goal = r.goal() == null ? META_PER_PROMOTER : r.goal();
    return build.insertPosition(
        coordinator.structureId(),
        "PROMOTER",
        coordinator.id(),
        section,
        goal,
        r.childLimit(),
        "MOV");
  }

  // ---------------------------------------------------------------- vacantes

  /**
   * Crea debajo del puesto las vacantes que faltan para cubrir la meta. Idempotente: cuenta los
   * hijos vigentes y sólo crea la diferencia.
   */
  @Transactional
  public List<VacancyResult> vacancies(Long positionId, List<SectionMeta> sections) {
    PositionRow position =
        build
            .position(positionId)
            .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));
    writableStructure(position.structureId());
    List<VacancyResult> out = new ArrayList<>();

    switch (Level.valueOf(position.level())) {
      case REFERENT -> {
        if (sections == null || sections.isEmpty()) {
          throw StructureException.badRequest("faltan las secciones con su meta");
        }
        for (SectionMeta s : sections) {
          TerritoryRow section = section(s.sectionGeoId());
          int needed = ceilDiv(s.meta(), META_PER_COORDINATOR);
          int existing = build.countChildren(position.id(), "ZONE_COORDINATOR", section.id());
          int created = 0;
          for (int i = existing; i < needed; i++) {
            build.insertPosition(
                position.structureId(),
                "ZONE_COORDINATOR",
                position.id(),
                section,
                META_PER_COORDINATOR,
                null,
                "CZ");
            created++;
          }
          out.add(new VacancyResult(s.sectionGeoId(), needed, existing, created));
        }
      }
      case ZONE_COORDINATOR -> {
        int goal = position.goal() == null ? META_PER_COORDINATOR : position.goal();
        int needed = ceilDiv(goal, META_PER_PROMOTER);
        int existing = build.countChildren(position.id(), "PROMOTER", null);
        TerritoryRow section = territoryOf(position);
        int created = 0;
        for (int i = existing; i < needed; i++) {
          build.insertPosition(
              position.structureId(),
              "PROMOTER",
              position.id(),
              section,
              META_PER_PROMOTER,
              null,
              "MOV");
          created++;
        }
        out.add(new VacancyResult(position.sectionGeoId(), needed, existing, created));
      }
      case PROMOTER -> throw StructureException.badRequest(
          "un movilizador no tiene puestos debajo");
    }
    log.info(
        "vacantes bajo el puesto {}: {} creadas",
        positionId,
        out.stream().mapToInt(VacancyResult::created).sum());
    return out;
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
    String role = roleFor(Level.valueOf(position.level()), structure.type());

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
    String grant = roleFor(Level.valueOf(position.level()), structure.type());
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
            .map(h -> roleFor(Level.valueOf(h.getLevel()), h.getStructureType()))
            .collect(Collectors.toSet());
    stillNeeded.add(grant);
    List<String> revoke =
        sameBranch.stream()
            .map(h -> roleFor(Level.valueOf(h.getLevel()), h.getStructureType()))
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

  /** El rol del realm que le toca al titular. Los nombres van con las erratas del realm. */
  static String roleFor(Level level, String structureType) {
    boolean alternate = "ALTERNATE".equals(structureType);
    return switch (level) {
      case REFERENT -> alternate ? "CORDINATOR_MUNICIPAL_ALT" : "CORDINATOR_MUNICIPAL";
      case ZONE_COORDINATOR -> alternate ? "CORDINATOR_ZONE_ALT" : "CORDINATOR_ZONE";
      case PROMOTER -> "PROMOTER";
    };
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

  private PositionRow parent(Long parentId, Level expected) {
    if (parentId == null) {
      throw StructureException.badRequest("falta el puesto del que cuelga");
    }
    PositionRow parent =
        build
            .position(parentId)
            .orElseThrow(() -> StructureException.notFound("el puesto vigente", parentId));
    if (!expected.name().equals(parent.level())) {
      throw StructureException.badRequest(
          "ese puesto cuelga de un " + expected.name() + ", no de un " + parent.level());
    }
    writableStructure(parent.structureId());
    return parent;
  }

  private TerritoryRow section(Integer sectionGeoId) {
    return build
        .sectionTerritory(sectionGeoId)
        .orElseThrow(() -> StructureException.notFound("la sección", sectionGeoId));
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
