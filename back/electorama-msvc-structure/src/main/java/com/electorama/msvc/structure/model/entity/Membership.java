package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.MembershipSource;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El convencido: la relación de una persona con una estructura, a través de un puesto.
 *
 * <p>Los dos punteros tienen propósitos distintos y no deben confundirse:
 *
 * <ul>
 *   <li>{@code puesto} — responsabilidad ACTUAL. Cambia sólo con un traslado explícito.
 *   <li>{@code capturedByAssignment} — linaje. INMUTABLE: un trigger impide reescribirlo, para que
 *       no vuelva a pasar lo del modelo viejo, donde la baja ponía {@code user_create_id = null} y
 *       se perdía de dónde venía el convencido.
 * </ul>
 *
 * <p>Apuntar a la asignación y no a la persona responde a la vez quién lo captó y desde qué puesto
 * lo hacía en ese momento.
 */
@Entity
@Table(name = "membership")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Membership {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id")
  private Person person;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "structure_id")
  private Structure structure;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "position_id")
  private Position position;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "captured_by_assignment_id", updatable = false)
  private Assignment capturedByAssignment;

  /** Dónde vive el convencido, que no tiene por qué ser el ámbito de su puesto. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "territory_id")
  private Territory territory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MembershipSource source;

  @Column(nullable = false)
  private String status;

  @Column(name = "vote_flag", nullable = false)
  private Boolean voteFlag;

  @Column(name = "vote_flag_timestamp")
  private OffsetDateTime voteFlagTimestamp;

  @Column(name = "legacy_id")
  private Long legacyId;
}
