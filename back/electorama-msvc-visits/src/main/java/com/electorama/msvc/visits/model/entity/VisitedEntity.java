package com.electorama.msvc.visits.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@Setter
@Entity
@Table(name = "visited")
public class VisitedEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotNull private Long municipalityId;

  @NotNull private Long seccionId;

  @Column(name = "coordinate_zone_id")
  private String coordinateZoneId;

  @NotNull private Long localityId;

  @NotNull private Date visitDate;

  private Integer numberOfVisited;

  @CreationTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "created")
  private Date created;

  @UpdateTimestamp
  @Temporal(TemporalType.TIMESTAMP)
  @Column(name = "updated")
  private Date updated;

  private Date deleted;

  @NotBlank private String userCreatedId;

  @OneToMany(fetch = FetchType.EAGER, mappedBy = "visitedEntity", cascade = CascadeType.ALL)
  @JsonIgnoreProperties(
      value = {"visitedEntity", "hibernateLazyInitializer", "handler"},
      allowSetters = true)
  private List<VisitedPeopleEntity> visitedPeople;

  private String referenceAuthId;
}
