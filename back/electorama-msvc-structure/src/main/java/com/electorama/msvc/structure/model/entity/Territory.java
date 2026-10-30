package com.electorama.msvc.structure.model.entity;

import com.electorama.msvc.structure.model.enums.TerritoryType;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * El territorio, como árbol propio y subdivisible. El catálogo del INE llega hasta la sección, pero
 * la operación necesita bajar más: la sección 30301 se reparte en 30301-a y 30301-b.
 *
 * <p>Los campos {@code *GeoId} son REFERENCIAS al catálogo que vive en msvc-geography. Nunca una
 * copia: ya hay ocho servicios que copian el catálogo como columnas denormalizadas.
 */
@Entity
@Table(name = "territory")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Territory {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TerritoryType type;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_territory_id")
  private Territory parentTerritory;

  @Column(nullable = false)
  private String code;

  private String name;

  @Column(name = "state_geo_id")
  private Integer stateGeoId;

  @Column(name = "municipality_geo_id")
  private Integer municipalityGeoId;

  @Column(name = "section_geo_id")
  private Integer sectionGeoId;

  @Column(name = "valid_from", insertable = false, updatable = false)
  private OffsetDateTime validFrom;

  @Column(name = "valid_to")
  private OffsetDateTime validTo;
}
