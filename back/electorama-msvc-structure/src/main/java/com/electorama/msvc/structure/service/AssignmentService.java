package com.electorama.msvc.structure.service;

import com.electorama.msvc.structure.exception.StructureException;
import com.electorama.msvc.structure.model.dto.RotateRequest;
import com.electorama.msvc.structure.model.entity.Person;
import com.electorama.msvc.structure.model.projection.AssignmentHistory;
import com.electorama.msvc.structure.model.repository.AssignmentRepository;
import com.electorama.msvc.structure.model.repository.MembershipRepository;
import com.electorama.msvc.structure.model.repository.PersonRepository;
import com.electorama.msvc.structure.model.repository.PositionRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rotar gente sin mover convencidos.
 *
 * <p>Es la razón de ser del modelo: el convencido se ancla al PUESTO, así que cambiar de titular no
 * toca ni una membresía. Lo comprueba la prueba 8 de {@code T3__compat_tests.sql} y lo vuelve a
 * comprobar {@link #rotate} devolviendo el conteo de antes y de después.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AssignmentService {

  private final AssignmentRepository assignments;
  private final PositionRepository positions;
  private final PersonRepository persons;
  private final MembershipRepository memberships;

  @Transactional(readOnly = true)
  public List<AssignmentHistory> history(Long positionId) {
    positions
        .summary(positionId)
        .orElseThrow(() -> StructureException.notFound("el puesto", positionId));
    return assignments.history(positionId);
  }

  /**
   * Cierra la asignación vigente y abre la nueva, en una sola transacción.
   *
   * <p>El orden importa: {@code assignment_single_holder} prohíbe dos titulares solapados en el
   * mismo puesto, así que primero se cierra y luego se abre. Si algo falla, la transacción deja el
   * puesto con su titular original.
   */
  @Transactional
  public long rotate(Long positionId, RotateRequest request) {
    positions
        .findByIdAndValidToIsNull(positionId)
        .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));

    if (request == null || request.personId() == null) {
      throw StructureException.badRequest("falta la persona que entra al puesto");
    }
    Person entrante =
        persons
            .findById(request.personId())
            .orElseThrow(() -> StructureException.notFound("la persona", request.personId()));

    long antes = memberships.countInSubtree(positionId);
    String reason = request.reason() == null ? "rotación" : request.reason();

    assignments.closeHolder(positionId, reason);
    assignments.openHolder(
        positionId, entrante.getId(), request.authId(), reason, entrante.getId());

    long despues = memberships.countInSubtree(positionId);
    if (antes != despues) {
      // No debería poder pasar: el convencido cuelga del puesto, no de la persona. Si pasa, es un
      // defecto del modelo y hay que enterarse en el acto, no en un reporte tres semanas después.
      throw StructureException.conflict(
          "la rotación movió convencidos (" + antes + " → " + despues + "): se revierte");
    }
    log.info("puesto {} rotado a la persona {}: {}", positionId, entrante.getId(), reason);
    return despues;
  }

  /**
   * Deja vacantes otros puestos de la misma rama y nombra en el nuevo, todo o nada: si el
   * nombramiento falla, los puestos no se quedan sin titular.
   */
  @Transactional
  public long vacateAndRotate(
      List<Long> vacate, String reason, Long positionId, RotateRequest request) {
    return vacateAndRotate(vacate, reason, positionId, List.of(), request);
  }

  /**
   * Lo mismo, y además nombra a la misma persona en {@code alsoFill} (los demás puestos de la
   * sección, con «toda la sección»): todo o nada.
   */
  @Transactional
  public long vacateAndRotate(
      List<Long> vacate,
      String reason,
      Long positionId,
      List<Long> alsoFill,
      RotateRequest request) {
    for (Long seat : vacate) {
      vacate(seat, reason);
    }
    long total = rotate(positionId, request);
    for (Long seat : alsoFill) {
      total += rotate(seat, request);
    }
    return total;
  }

  /** Deja el puesto sin titular, que el modelo permite: un puesto puede estar vacante. */
  @Transactional
  public void vacate(Long positionId, String reason) {
    positions
        .findByIdAndValidToIsNull(positionId)
        .orElseThrow(() -> StructureException.notFound("el puesto vigente", positionId));
    if (assignments.currentHolderId(positionId).isEmpty()) {
      throw StructureException.conflict("el puesto ya está vacante");
    }
    assignments.closeHolder(positionId, reason == null ? "baja del titular" : reason);
  }
}
