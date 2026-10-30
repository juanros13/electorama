package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.CaptureRequests.Capture;
import com.electorama.msvc.structure.model.dto.CaptureRequests.CaptureTarget;
import com.electorama.msvc.structure.model.dto.CaptureRequests.Captured;
import com.electorama.msvc.structure.model.repository.CaptureRepository;
import com.electorama.msvc.structure.model.repository.CaptureRepository.TargetRow;
import com.electorama.msvc.structure.security.BranchGuard;
import com.electorama.msvc.structure.service.rest.GeographyClient;
import feign.FeignException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * La captura de convencidos desde el front, directo al modelo de estructura (sin msvc-followers).
 *
 * <p>Las reglas son las de la app: si la persona simpatiza se guardan todos sus datos y la
 * encuesta; si no, sólo domicilio, sección y sexo. La persona se reutiliza por CURP (el padrón es
 * compartido entre campañas) y en una estructura sólo puede ser convencido de un puesto a la vez.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptureService {

  private static final Pattern CURP =
      Pattern.compile(
          "^[A-Z][AEIOUX][A-Z]{2}\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])[HM][A-Z]{2}"
              + "[B-DF-HJ-NP-TV-Z]{3}[0-9A-Z]\\d$");
  private static final Pattern INE = Pattern.compile("^[A-Z]{6}\\d{8}[A-Z]\\d{3}$");
  private static final Pattern PHONE = Pattern.compile("^\\d{10}$");
  /** El margen del GPS de un teléfono: un punto a esta distancia del borde cuenta como dentro. */
  static final double SECTION_TOLERANCE_METERS = 100;

  private static final Set<String> INVITED =
      Set.of("uno", "dos", "tres", "cuatro", "cinco", "masdecinco");

  private final CaptureRepository capture;
  private final BranchGuard branch;
  private final GeographyClient geography;

  /** Dónde puede dejar convencidos quien llama. */
  @Transactional(readOnly = true)
  public List<CaptureTarget> targets() {
    String authId = branch.callerAuthId();
    return authId == null ? List.of() : capture.targets(authId);
  }

  @Transactional
  public Captured capture(Capture r) {
    if (r == null) {
      throw StructureException.badRequest("faltan los datos del convencido");
    }
    String authId = branch.callerAuthId();
    TargetRow target = resolveTarget(r.positionId(), authId);

    boolean supports = !Boolean.FALSE.equals(r.supportsMovement());
    String curp = upper(r.curp());
    String ine = upper(r.ine());
    validate(r, supports, curp, ine);

    Integer sectionGeoId = r.sectionGeoId() != null ? r.sectionGeoId() : target.sectionGeoId();
    if (sectionGeoId == null) {
      throw StructureException.badRequest("falta la sección donde vive");
    }
    requireInsideTerritory(target, sectionGeoId);
    requirePointInSection(sectionGeoId, r.latitude(), r.longitude());
    Long territory =
        capture
            .sectionTerritory(sectionGeoId)
            .orElseThrow(() -> StructureException.badRequest("la sección no está en el catálogo"));

    // sin simpatía, como en la app: sólo domicilio, sección y sexo
    MapSqlParameterSource person =
        new MapSqlParameterSource()
            .addValue("curp", supports ? curp : null)
            .addValue("ine", supports ? ine : null)
            .addValue("names", supports ? upper(r.names()) : null)
            .addValue("lastName", supports ? upper(r.lastName()) : null)
            .addValue("surName", supports ? upper(r.surName()) : null)
            .addValue("birthdate", supports ? r.birthdate() : null)
            .addValue("phone", supports ? blankToNull(r.cellPhoneNumber()) : null)
            .addValue("phoneOther", supports ? blankToNull(r.cellPhoneNumberOther()) : null)
            .addValue("email", supports ? lower(r.email()) : null)
            .addValue("street", upper(r.street()))
            .addValue("externalNumber", upper(r.externalNumber()))
            .addValue("internalNumber", upper(r.internalNumber()))
            .addValue("neighborhood", upper(r.neighborhood()))
            .addValue("zipCode", blankToNull(r.zipCode()))
            .addValue("ineValidity", supports ? r.ineValidity() : null);

    Optional<Long> existing =
        curp == null || !supports ? Optional.empty() : capture.personByCurp(curp);
    long personId;
    if (existing.isPresent()) {
      personId = existing.get();
      capture
          .activeMembershipLabel(personId, target.structureId())
          .ifPresent(
              where -> {
                throw new StructureException(
                    HttpStatus.CONFLICT,
                    "ya es convencido en esta estructura: " + where,
                    Map.of("code", "ALREADY_SUPPORTER", "where", where));
              });
      capture.fillPerson(personId, person);
    } else {
      personId = capture.insertPerson(person);
    }

    long membershipId =
        capture.insertMembership(
            new MapSqlParameterSource()
                .addValue("person", personId)
                .addValue("structure", target.structureId())
                .addValue("position", target.id())
                .addValue("territory", territory)
                .addValue(
                    "assignment",
                    authId == null
                        ? null
                        : capture.callerAssignment(authId, target.structureId()).orElse(null))
                .addValue("authId", authId));

    capture.insertDetail(
        new MapSqlParameterSource()
            .addValue("membership", membershipId)
            .addValue("sexId", sexId(r.sex()))
            .addValue("supports", supports ? "SI" : "NO")
            .addValue("committee", supports ? siNo(r.committeeParticipation()) : null)
            .addValue("pollingRep", supports ? siNo(r.pollingRepParticipation()) : null)
            .addValue("inviting", supports ? siNo(r.invitingOthers()) : null)
            .addValue(
                "invited",
                supports && Boolean.TRUE.equals(r.invitingOthers())
                    ? r.numberOfPeopleInvited()
                    : null)
            .addValue("whatsapp", supports ? siNo(r.supportWhatsapp()) : null)
            .addValue("ineValidity", supports ? r.ineValidity() : null)
            // el mismo formato de texto que guardaba la app
            .addValue("latitude", r.latitude() == null ? null : r.latitude().toString())
            .addValue("longitude", r.longitude() == null ? null : r.longitude().toString()));

    String label = capture.positionLabel(target.id());
    log.info(
        "convencido {} capturado en el puesto {} ({}) por {}",
        membershipId,
        target.id(),
        label,
        authId);
    return new Captured(membershipId, personId, target.id(), label, existing.isEmpty());
  }

  /**
   * El convencido es de la sección del puesto: un promotor o movilizador de la 233 sólo capta en la
   * 233; un referente, en cualquier sección de su municipio.
   */
  private void requireInsideTerritory(TargetRow target, Integer sectionGeoId) {
    if (target.sectionGeoId() != null) {
      if (!target.sectionGeoId().equals(sectionGeoId)) {
        throw StructureException.badRequest("el convencido tiene que ser de la sección del puesto");
      }
      return;
    }
    if (target.municipalityGeoId() != null) {
      Integer mun = capture.sectionMunicipality(sectionGeoId).orElse(null);
      if (!target.municipalityGeoId().equals(mun)) {
        throw StructureException.badRequest(
            "el convencido tiene que ser de una sección del municipio del puesto");
      }
    }
  }

  /**
   * La ubicación tiene que caer dentro de la sección o a no más de 100 m de su borde (el error del
   * GPS). Si la sección no tiene geometría no se puede decir que esté fuera y pasa; si geography no
   * contesta, no se guarda.
   */
  private void requirePointInSection(Integer sectionGeoId, Double lat, Double lng) {
    if (lat == null || lng == null) {
      return;
    }
    Object inside;
    try {
      inside =
          geography.sectionContains(sectionGeoId, lat, lng, SECTION_TOLERANCE_METERS).get("inside");
    } catch (FeignException e) {
      log.warn(
          "geography no dijo si el punto cae en la sección {}: {}", sectionGeoId, e.getMessage());
      throw new StructureException(
          HttpStatus.BAD_GATEWAY, "no se pudo comprobar la ubicación; intenta de nuevo");
    }
    if (Boolean.FALSE.equals(inside)) {
      throw new StructureException(
          HttpStatus.BAD_REQUEST,
          "la ubicación está fuera de la sección",
          Map.of("code", "OUTSIDE_SECTION"));
    }
  }

  /** El puesto pedido (de su rama) o, si no pide, el suyo de la campaña activa. */
  private TargetRow resolveTarget(Long positionId, String authId) {
    Long id = positionId;
    if (id == null) {
      List<CaptureTarget> own =
          authId == null
              ? List.of()
              : capture.targets(authId).stream().filter(CaptureTarget::own).toList();
      if (own.size() != 1) {
        throw StructureException.badRequest(
            own.isEmpty()
                ? "no ocupas ningún puesto en la campaña activa"
                : "ocupas varios puestos: elige en cuál queda el convencido");
      }
      id = own.get(0).positionId();
    }
    if (!branch.canRead(id)) {
      throw new StructureException(HttpStatus.FORBIDDEN, "ese puesto no es de tu rama");
    }
    final Long target = id;
    TargetRow row =
        capture.target(target).orElseThrow(() -> StructureException.notFound("el puesto", target));
    if ("CLOSED".equals(row.campaignStatus())) {
      throw StructureException.conflict("la campaña del puesto está cerrada: sólo se consulta");
    }
    return row;
  }

  private void validate(Capture r, boolean supports, String curp, String ine) {
    if ((r.latitude() == null) != (r.longitude() == null)) {
      throw StructureException.badRequest("la ubicación lleva latitud y longitud");
    }
    // un punto fuera de México es un error del GPS o de un clic, no un domicilio
    if (r.latitude() != null
        && (r.latitude() < 14
            || r.latitude() > 33
            || r.longitude() < -118
            || r.longitude() > -86)) {
      throw StructureException.badRequest("la ubicación está fuera de México");
    }
    if (sexId(r.sex()) == null) {
      throw StructureException.badRequest("falta el sexo (HOMBRE o MUJER)");
    }
    if (!supports) {
      return;
    }
    if (isBlank(r.names()) || isBlank(r.lastName())) {
      throw StructureException.badRequest("falta el nombre y el primer apellido");
    }
    if (curp == null || !CURP.matcher(curp).matches()) {
      throw StructureException.badRequest("la CURP no es válida");
    }
    if (ine != null && !INE.matcher(ine).matches()) {
      throw StructureException.badRequest("la clave de elector no es válida (18 caracteres)");
    }
    if (!isBlank(r.cellPhoneNumber()) && !PHONE.matcher(r.cellPhoneNumber().trim()).matches()) {
      throw StructureException.badRequest("el teléfono lleva 10 dígitos");
    }
    if (Boolean.TRUE.equals(r.invitingOthers())
        && (r.numberOfPeopleInvited() == null || !INVITED.contains(r.numberOfPeopleInvited()))) {
      throw StructureException.badRequest("falta a cuántas personas invita");
    }
  }

  private static Long sexId(String sex) {
    if ("HOMBRE".equalsIgnoreCase(sex)) {
      return 1L;
    }
    if ("MUJER".equalsIgnoreCase(sex)) {
      return 2L;
    }
    return null;
  }

  private static String siNo(Boolean b) {
    return b == null ? null : (b ? "SI" : "NO");
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }

  private static String blankToNull(String s) {
    return isBlank(s) ? null : s.trim();
  }

  private static String upper(String s) {
    return isBlank(s) ? null : s.trim().toUpperCase();
  }

  private static String lower(String s) {
    return isBlank(s) ? null : s.trim().toLowerCase();
  }
}
