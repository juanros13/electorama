package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.AssignmentRole;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una persona ocupando un puesto durante un periodo.
 *
 * <p>Rotar a alguien es cerrar la asignación vigente y abrir otra: no se toca ni una fila de {@link
 * Membership}. La base impone con un EXCLUDE que no haya dos titulares solapados en el mismo
 * puesto.
 *
 * <p>La vigencia es un {@code tstzrange} de PostgreSQL; se lee y escribe por los extremos ({@code
 * validFrom} / {@code validTo}) a través de la vista de rangos. Para rangos abiertos, {@code
 * validTo} es null.
 */
@Entity
@Table(name = "assignment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assignment {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "position_id")
  private Position position;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "person_id")
  private Person person;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AssignmentRole role;

  @Column(name = "start_reason")
  private String startReason;

  @Column(name = "end_reason")
  private String endReason;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_person_id")
  private Person createdBy;

  @Column(name = "created", insertable = false, updatable = false)
  private OffsetDateTime created;
}
