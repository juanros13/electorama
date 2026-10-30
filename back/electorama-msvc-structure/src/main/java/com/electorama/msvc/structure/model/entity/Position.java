package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.Level;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El esqueleto estable: lo que NO se mueve cuando rota la gente.
 *
 * <p>Lleva dos jerarquías independientes: {@code parentPosition} es el ORGANIGRAMA y {@code ambito}
 * es el TERRITORIO. Que sean independientes es lo que permite repartir una sección entre promotores
 * que cuelgan de coordinadores de zona distintos.
 *
 * <p>Un puesto nunca se borra: se cierra poniendo {@code validTo}. Las reglas duras (el padre debe
 * ser de level superior, misma estructura, ámbito contenido en el del padre) las impone un trigger
 * en la base, no esta clase.
 */
@Entity
@Table(name = "position")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Position {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "structure_id")
  private Structure structure;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Level level;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_position_id")
  private Position parentPosition;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "territory_id")
  private Territory territory;

  private String code;

  /** Meta de captación del puesto. Vive aquí, no en el territorio. */
  private Integer goal;

  /** Tope de puestos hijos (el viejo {@code limit_promoters}). */
  @Column(name = "child_limit")
  private Integer childLimit;

  @Column(name = "valid_from", insertable = false, updatable = false)
  private OffsetDateTime validFrom;

  @Column(name = "valid_to")
  private OffsetDateTime validTo;
}
