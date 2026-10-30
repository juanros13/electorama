package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.StructureType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reemplaza al booleano {@code promoters.flag_alter}. La principal, la alterna y cualquier aliada
 * futura son filas, no ramas de código — con eso desaparece la duplicación de clases {@code *Alt*}
 * que ya produjo bugs de copy-paste.
 */
@Entity
@Table(name = "structure")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Structure {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "campaign_id")
  private Campaign campaign;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private StructureType type;

  /** Quién dirige esta estructura. Es lo que permite una alterna a cargo de alguien más. */
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_person_id")
  private Person ownerPerson;

  @Column(name = "valid_from", insertable = false, updatable = false)
  private OffsetDateTime validFrom;

  @Column(name = "valid_to")
  private OffsetDateTime validTo;
}
