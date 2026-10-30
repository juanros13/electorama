package com.electorama.msvc.structure.model.repository;

import com.electorama.msvc.structure.model.entity.Assignment;
import com.electorama.msvc.structure.model.projection.AssignmentHistory;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Las asignaciones se consultan y se cierran en SQL nativo: la vigencia es un {@code TSTZRANGE} y
 * JPA no lo mapea. La restricción {@code assignment_single_holder} garantiza que no haya dos
 * titulares solapados en el mismo puesto, así que rotar es cerrar la vigente y abrir la nueva —en
 * ese orden y en la misma transacción.
 */
@Repository
public interface AssignmentRepository extends JpaRepository<Assignment, Long> {

  @Query(
      value =
          "SELECT id FROM assignment"
              + " WHERE position_id = :positionId AND role = 'HOLDER'"
              + "   AND upper(validity) IS NULL LIMIT 1",
      nativeQuery = true)
  Optional<Long> currentHolderId(@Param("positionId") Long positionId);

  @Query(
      value =
          "SELECT a.id AS id, a.person_id AS personId, a.auth_id AS authId,"
              + "       trim(concat_ws(' ', p.names, p.last_name, p.sur_name)) AS personName,"
              + "       a.role AS role, lower(a.validity) AS validFrom, upper(a.validity) AS validTo,"
              + "       a.start_reason AS startReason, a.end_reason AS endReason"
              + " FROM assignment a JOIN person p ON p.id = a.person_id"
              + " WHERE a.position_id = :positionId"
              + " ORDER BY lower(a.validity) DESC, a.id DESC",
      nativeQuery = true)
  List<AssignmentHistory> history(@Param("positionId") Long positionId);

  /**
   * Cierra la asignación vigente del puesto.
   *
   * <p>{@code clock_timestamp()} y un {@code GREATEST} contra el inicio: con {@code now()}, cerrar
   * algo dado de alta en la misma transacción produce {@code tstzrange(t, t)} —el rango vacío, cuyo
   * {@code upper()} es NULL— y la baja no daría de baja.
   */
  @Modifying
  @Query(
      value =
          "UPDATE assignment"
              + "   SET validity = tstzrange(lower(validity),"
              + "         GREATEST(clock_timestamp(), lower(validity) + interval '1 microsecond')),"
              + "       end_reason = :reason"
              + " WHERE position_id = :positionId AND role = 'HOLDER'"
              + "   AND upper(validity) IS NULL",
      nativeQuery = true)
  int closeHolder(@Param("positionId") Long positionId, @Param("reason") String reason);

  /** Abre la asignación nueva. La vigencia arranca con {@code clock_timestamp()} por lo mismo. */
  @Modifying
  @Query(
      value =
          "INSERT INTO assignment (position_id, person_id, role, auth_id, validity,"
              + "                        start_reason, created_by_person_id)"
              + " VALUES (:positionId, :personId, 'HOLDER', :authId,"
              + "         tstzrange(clock_timestamp(), NULL), :reason, :createdBy)",
      nativeQuery = true)
  int openHolder(
      @Param("positionId") Long positionId,
      @Param("personId") Long personId,
      @Param("authId") String authId,
      @Param("reason") String reason,
      @Param("createdBy") Long createdBy);
}
