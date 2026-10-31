package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.CampaignStatus;
import com.electorama.msvc.structure.model.enums.CampaignType;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El contenedor que permite reutilizar el modelo en la elección siguiente.
 *
 * <p>Personas y ámbitos son transversales (se reutilizan); estructuras, puestos, asignaciones y
 * adhesiones son por campaña. Un convencido de 2024 no es automáticamente convencido de 2027.
 */
@Entity
@Table(name = "campaign")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CampaignType type;

  private Integer year;

  @Column(name = "election_day")
  private LocalDate electionDay;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "territory_id")
  private Territory territory;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CampaignStatus status;

  /** Inicio y cierre de la campaña (V3); el día de la elección va aparte. */
  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  private String description;

  /** % de la LN que es meta en cada sección (V6); se aplica a geography desde la configuración. */
  @Column(name = "goal_percentage", nullable = false)
  private BigDecimal goalPercentage;

  @Column(name = "valid_from", insertable = false, updatable = false)
  private OffsetDateTime validFrom;

  @Column(name = "valid_to")
  private OffsetDateTime validTo;
}
