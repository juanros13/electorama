package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.ChangeType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bitácora append-only, escrita por el backend dentro de la misma transacción que el cambio.
 *
 * <p>Sustituye al microservicio {@code logfile}, que recibe texto libre enviado desde el navegador:
 * si alguien llama la API directo, ahí no queda rastro.
 */
@Entity
@Table(name = "change_log")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeLog {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChangeType type;

  @Column(nullable = false)
  private String entity;

  @Column(name = "entity_id")
  private Long entityId;

  @Column(columnDefinition = "jsonb")
  private String beforeData;

  @Column(columnDefinition = "jsonb")
  private String afterData;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "actor_person_id")
  private Person actorPerson;

  private String reason;

  @Column(name = "occurred_at", insertable = false, updatable = false)
  private OffsetDateTime occurredAt;
}
