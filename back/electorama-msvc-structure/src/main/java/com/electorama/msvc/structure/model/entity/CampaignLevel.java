package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.Level;
import com.electorama.msvc.structure.model.enums.StructureType;
import com.electorama.msvc.structure.model.enums.TerritoryType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un nivel del organigrama de una campaña (V3): cuántos hay, cómo se llaman, dónde se coloca cada
 * puesto, con qué meta nace y qué rol de Keycloak recibe su titular.
 *
 * <p>{@code levelFunction} liga el nivel con la lógica heredada de los tres niveles fijos; un nivel
 * sin función sólo agrega jerarquía.
 */
@Entity
@Table(name = "campaign_level")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampaignLevel {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "campaign_id", nullable = false)
  private Long campaignId;

  /** 1 es la cúspide. */
  @Column(nullable = false)
  private Short rank;

  @Column(nullable = false)
  private String code;

  @Column(name = "name_singular", nullable = false)
  private String nameSingular;

  @Column(name = "name_plural", nullable = false)
  private String namePlural;

  /** Dónde se coloca el puesto; null = donde diga quien lo crea. */
  @Enumerated(EnumType.STRING)
  @Column(name = "territory_type")
  private TerritoryType territoryType;

  @Column(name = "default_goal")
  private Integer defaultGoal;

  @Column(name = "default_child_limit")
  private Integer defaultChildLimit;

  @Enumerated(EnumType.STRING)
  @Column(name = "level_function")
  private Level levelFunction;

  @Column(name = "role_name", nullable = false)
  private String roleName;

  @Column(name = "alternate_role_name")
  private String alternateRoleName;

  @Column(insertable = false, updatable = false)
  private OffsetDateTime created;

  /** El rol que recibe el titular: el alterno en una estructura ALTERNATE, si lo hay. */
  public String roleFor(StructureType structureType) {
    return structureType == StructureType.ALTERNATE && alternateRoleName != null
        ? alternateRoleName
        : roleName;
  }
}
